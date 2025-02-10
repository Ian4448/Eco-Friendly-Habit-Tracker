let map;
let currentMarker;
let clickedMarker;
let currentPosition;
let startMarker, endMarker;
let selectedTransport = null;
let directionsService;
let directionsRenderer;
let activeVehicle = null;
let isDragging = false;
let currentX, currentY;
let initialX, initialY;
let xOffset = 0, yOffset = 0;
let tripPlanner = null;
let locationSelectModal = null;
let clickedLatLng = null;

function createLocationModal() {
    const modal = document.createElement('div');
    modal.className = 'location-select-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <h3>Set Location As</h3>
            <div class="modal-buttons">
                <button class="modal-btn start-btn">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                    Start Location
                </button>
                <button class="modal-btn end-btn">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                        <circle cx="12" cy="10" r="3"></circle>
                    </svg>
                    Destination
                </button>
            </div>
            <button class="modal-btn cancel-btn">Cancel</button>
        </div>
    `;
    document.body.appendChild(modal);

    // Add event listeners for the buttons
    modal.querySelector('.start-btn').addEventListener('click', () => {
        setLocationFromClick('start');
        hideLocationModal();
    });

    modal.querySelector('.end-btn').addEventListener('click', () => {
        setLocationFromClick('end');
        hideLocationModal();
    });

    modal.querySelector('.cancel-btn').addEventListener('click', hideLocationModal);

    return modal;
}

function showLocationModal(position) {
    if (!locationSelectModal) {
        locationSelectModal = createLocationModal();
    }

    clickedLatLng = position;
    locationSelectModal.style.display = 'flex';
}

function hideLocationModal() {
    if (locationSelectModal) {
        locationSelectModal.style.display = 'none';
    }
}

function setLocationFromClick(type) {
    if (!clickedLatLng) return;

    if (type === 'start') {
        if (startMarker) startMarker.setMap(null);
        startMarker = new google.maps.Marker({
            position: clickedLatLng,
            map: map,
            label: {
                text: 'A',
                className: 'marker-label'
            }
        });
        updateLocationInput('start-location', clickedLatLng);
    } else {
        if (endMarker) endMarker.setMap(null);
        endMarker = new google.maps.Marker({
            position: clickedLatLng,
            map: map,
            label: {
                text: 'B',
                className: 'marker-label'
            }
        });
        updateLocationInput('end-location', clickedLatLng);
    }

    checkCalculateEnabled();
}

function initDraggable() {
    tripPlanner = document.querySelector('.trip-planner');

    // Hide initially
    tripPlanner.style.visibility = 'hidden';

    const dragHandle = document.createElement('div');
    dragHandle.className = 'drag-handle';

    tripPlanner.insertBefore(dragHandle, tripPlanner.firstChild);

    // Set fixed position before calculating dimensions
    tripPlanner.style.position = 'fixed';

    // Calculate and set initial position immediately
    const padding = 40;
    const initialX = padding;
    const initialY = window.innerHeight - tripPlanner.offsetHeight - padding;

    // Set initial transform immediately
    setTranslate(initialX, initialY, tripPlanner);

    // Update tracking variables
    xOffset = initialX;
    yOffset = initialY;
    currentX = initialX;
    currentY = initialY;

    // Show the element after position is set
    requestAnimationFrame(() => {
        tripPlanner.style.visibility = 'visible';
    });

    // Event listeners for dragging
    dragHandle.addEventListener('mousedown', dragStart);
    document.addEventListener('mousemove', drag);
    document.addEventListener('mouseup', dragEnd);

    dragHandle.addEventListener('touchstart', dragStart);
    document.addEventListener('touchmove', drag);
    document.addEventListener('touchend', dragEnd);

    // Add window resize handler
    window.addEventListener('resize', () => {
        if (!isDragging) {
            const padding = 40;
            const maxX = window.innerWidth - tripPlanner.offsetWidth - padding;
            const maxY = window.innerHeight - tripPlanner.offsetHeight - padding;

            xOffset = Math.min(Math.max(xOffset, padding), maxX);
            yOffset = Math.min(Math.max(yOffset, padding), maxY);

            setTranslate(xOffset, yOffset, tripPlanner);
        }
    });
}

function drag(e) {
    if (isDragging) {
        e.preventDefault();

        if (e.type === 'touchmove') {
            currentX = e.touches[0].clientX - initialX;
            currentY = e.touches[0].clientY - initialY;
        } else {
            currentX = e.clientX - initialX;
            currentY = e.clientY - initialY;
        }

        const padding = 40;
        const maxX = window.innerWidth - tripPlanner.offsetWidth - padding;
        const maxY = window.innerHeight - tripPlanner.offsetHeight - padding;

        xOffset = Math.min(Math.max(currentX, padding), maxX);
        yOffset = Math.min(Math.max(currentY, padding), maxY);

        setTranslate(xOffset, yOffset, tripPlanner);
    }
}

function dragStart(e) {
    if (e.type === 'touchstart') {
        initialX = e.touches[0].clientX - xOffset;
        initialY = e.touches[0].clientY - yOffset;
    } else {
        initialX = e.clientX - xOffset;
        initialY = e.clientY - yOffset;
    }

    if (e.target.classList.contains('drag-handle')) {
        isDragging = true;
    }
}

function dragEnd(e) {
    initialX = currentX;
    initialY = currentY;
    isDragging = false;
}

function setTranslate(xPos, yPos, el) {
    el.style.transform = `translate(${xPos}px, ${yPos}px)`;
}


window.initMap = function() {
    // Initialize the map centered on a default location
    map = new google.maps.Map(document.getElementById('map'), {
        center: { lat: 0, lng: 0 },
        zoom: 2,
        styles: [
            {
                "featureType": "all",
                "elementType": "geometry",
                "stylers": [{ "color": "#E8F5E9" }]
            },
            {
                "featureType": "water",
                "elementType": "geometry",
                "stylers": [{ "color": "#B2DFDB" }]
            }
        ]
    });

    // Initialize Directions Service and Renderer
    directionsService = new google.maps.DirectionsService();
    directionsRenderer = new google.maps.DirectionsRenderer({
        suppressMarkers: true,
        polylineOptions: {
            strokeColor: '#4CAF50',
            strokeWeight: 4
        }
    });
    directionsRenderer.setMap(map);

    // Get user's location
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                currentPosition = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                };

                // Center map on user's location
                map.setCenter(currentPosition);
                map.setZoom(15);

                // Update status
                document.getElementById('location-status').innerHTML = 'Location found!';
                document.getElementById('location-status').style.background = '#E8F5E9';

                // Add marker for current location
                currentMarker = new google.maps.Marker({
                    position: currentPosition,
                    map: map,
                    title: 'Your Location',
                    icon: {
                        path: google.maps.SymbolPath.CIRCLE,
                        scale: 10,
                        fillColor: "#4CAF50",
                        fillOpacity: 1,
                        strokeWeight: 2,
                        strokeColor: "#FFFFFF"
                    }
                });

                // Update current location text
                updateLocationText();

                // Set current location as start position
                setCurrentLocationAsStart();
            },
            (error) => {
                document.getElementById('location-status').innerHTML =
                    'Error: The Geolocation service failed.';
                document.getElementById('location-status').style.background = '#FFEBEE';
            }
        );
    } else {
        document.getElementById('location-status').innerHTML =
            'Error: Your browser doesn\'t support geolocation.';
        document.getElementById('location-status').style.background = '#FFEBEE';
    }

    // Add click listener for map interactions
    map.addListener('click', (mapsMouseEvent) => {
        showLocationModal(mapsMouseEvent.latLng);
    });
}

function updateLocationInput(inputId, latLng) {
    fetch(`https://maps.googleapis.com/maps/api/geocode/json?latlng=${latLng.lat()},${latLng.lng()}&key=${config.GOOGLE_MAPS_API_KEY}`)
        .then(response => response.json())
        .then(data => {
            if (data.results && data.results[0]) {
                document.getElementById(inputId).value = data.results[0].formatted_address;
            }
        });
}

function selectTransport(mode) {
    selectedTransport = mode;
    document.querySelectorAll('.transport-option').forEach(option => {
        option.classList.remove('active');
    });
    document.querySelector(`[data-mode="${mode}"]`).classList.add('active');

    // Clear any existing error message
    const tripResult = document.getElementById('trip-result');
    tripResult.style.display = 'none';

    // If car is selected but no vehicles exist, show error
    if (mode === 'car') {
        const vehicleList = document.getElementById('vehicleList');
        const hasVehicles = vehicleList.querySelector('.vehicle-item') !== null;

        if (!hasVehicles) {
            tripResult.innerHTML = `
                <div style="color: #dc2626">⚠️ You need to add a vehicle first to use car mode</div>
                <a href="/createVehicle" style="display: inline-block; margin-top: 12px; padding: 8px 16px; background: #22c55e; color: white; text-decoration: none; border-radius: 8px;">
                    Add a Vehicle
                </a>
            `;
            tripResult.style.display = 'block';
            document.getElementById('confirm-trip').disabled = true;
            return;
        }
    }

    checkCalculateEnabled();
}

function checkCalculateEnabled() {
    const startInput = document.getElementById('start-location').value;
    const endInput = document.getElementById('end-location').value;
    const confirmButton = document.getElementById('confirm-trip');

    let isEnabled = startInput && endInput && selectedTransport;

    // For car mode, also check if there's a vehicle
    if (selectedTransport === 'car') {
        const vehicleList = document.getElementById('vehicleList');
        const hasVehicles = vehicleList.querySelector('.vehicle-item') !== null;
        isEnabled = isEnabled && hasVehicles;
    }

    confirmButton.disabled = !isEnabled;
}

function calculateTrip() {
    if (!startMarker || !endMarker) return;

    const request = {
        origin: startMarker.getPosition(),
        destination: endMarker.getPosition(),
        travelMode: getTravelMode()
    };

    directionsService.route(request, (result, status) => {
        if (status === 'OK') {
            directionsRenderer.setDirections(result);
            const distance = result.routes[0].legs[0].distance.text;
            const distanceInKm = parseFloat(distance.replace(' km', ''));

            const userId = document.cookie
                .split('; ')
                .find(row => row.startsWith('user_id='))
                ?.split('=')[1]; // Ensure optional chaining (?) is supported in your environment

            let transportationType;
            switch (selectedTransport) {
                case 'car':
                    transportationType = 'CAR';
                    break;
                case 'bike':
                    transportationType = 'BIKE';
                    break;
                case 'walk':
                    transportationType = 'WALK';
                    break;
                default:
                    console.error('Invalid transport type');
                    return;
            }

            const goodChoice = transportationType === 'BIKE' || transportationType === 'WALK';

            const requestBody = {
                time: 'DAILY',
                transportation: transportationType,
                userId: userId,
                vehicleName: activeVehicle || '',
                distanceTravelled: distanceInKm,
                goodChoice: goodChoice
            };

            fetch('/api/modifyUserEmission', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to log emission data');
                    }
                    return response.json().catch(() => ({})); // Handle empty response
                })
                .then(() => {
                    displayTripResult(distance, goodChoice);
                })
                .catch(error => {
                    console.error('Error logging emission data:', error);
                });
        }
    });
}


function getTravelMode() {
    switch(selectedTransport) {
        case 'car': return google.maps.TravelMode.DRIVING;
        case 'bike': return google.maps.TravelMode.BICYCLING;
        case 'walk': return google.maps.TravelMode.WALKING;
        default: return google.maps.TravelMode.DRIVING;
    }
}

function displayTripResult(distance, goodChoice) {
    const tripResult = document.getElementById('trip-result');
    let transportMode = selectedTransport;
    if (selectedTransport === 'car' && activeVehicle) {
        transportMode = `${activeVehicle} (car)`;
    }

    let impactMessage = goodChoice ?
        '🌿 Great choice! This eco-friendly option helps reduce emissions.' :
        '💡 Consider biking or walking for shorter trips to reduce your carbon footprint.';

    tripResult.innerHTML = `
        <div>Trip distance: ${distance}</div>
        <div>Transport mode: ${transportMode}</div>
        <div style="margin-top: 8px; color: ${goodChoice ? '#4CAF50' : '#FF9800'}">${impactMessage}</div>
    `;
    tripResult.style.display = 'block';
}

function updateLocationText() {
    if (currentPosition) {
        fetch(`https://maps.googleapis.com/maps/api/geocode/json?latlng=${currentPosition.lat},${currentPosition.lng}&key=${config.GOOGLE_MAPS_API_KEY}`)
            .then(response => response.json())
            .then(data => {
                if (data.results && data.results[0]) {
                    document.getElementById('current-location').innerHTML =
                        `Current Location: ${data.results[0].formatted_address}`;
                }
            })
            .catch(error => {
                console.error('Error fetching address:', error);
            });
    }
}

function setCurrentLocationAsStart() {
    if (currentPosition) {
        // Reverse geocode the current position to get the address
        fetch(`https://maps.googleapis.com/maps/api/geocode/json?latlng=${currentPosition.lat},${currentPosition.lng}&key=${config.GOOGLE_MAPS_API_KEY}`)
            .then(response => response.json())
            .then(data => {
                if (data.results && data.results[0]) {
                    const address = data.results[0].formatted_address;
                    document.getElementById('start-location').value = address;

                    // Create the start marker if it doesn't exist
                    if (!startMarker) {
                        startMarker = new google.maps.Marker({
                            position: currentPosition,
                            map: map,
                            label: {
                                text: 'A',
                                className: 'marker-label'
                            }
                        });
                    }

                    checkCalculateEnabled();
                }
            })
            .catch(error => {
                console.error('Error getting address:', error);
            });
    }
}

async function updateProfileLink() {
    try {
        const userId = document.cookie
            .split('; ')
            .find(row => row.startsWith('user_id='))
            ?.split('=')[1];
        if (userId) {
            const profileLink = document.getElementById('profileLink');
            if (profileLink) {
                profileLink.href = `/edit/${userId}`;
            }
        }
    } catch (error) {
        console.error('Error updating profile link:', error);
    }
}
async function loadVehicles() {
    try {
        const response = await fetch('/api/getVehicles');
        const vehicles = await response.json();
        const vehicleList = document.getElementById('vehicleList');

        if (!vehicles || vehicles.length === 0) {
            vehicleList.innerHTML = `
                <div style="text-align: center; padding: 24px; background: #f9fafb; border-radius: 12px; border: 1px solid #e5e7eb;">
                    <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/>
                        <circle cx="7" cy="17" r="2"/>
                        <circle cx="17" cy="17" r="2"/>
                    </svg>
                    <p style="margin: 16px 0; color: #374151;">Add your first vehicle to start tracking car trips</p>
                    <a href="/createVehicle" style="display: inline-flex; align-items: center; gap: 8px; padding: 12px 20px; background: #22c55e; color: white; text-decoration: none; border-radius: 8px; font-weight: 500;">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="12" y1="5" x2="12" y2="19"></line>
                            <line x1="5" y1="12" x2="19" y2="12"></line>
                        </svg>
                        Add Vehicle
                    </a>
                </div>
            `;

            // If car mode is selected, ensure calculate button is disabled
            if (selectedTransport === 'car') {
                document.getElementById('confirm-trip').disabled = true;
                const tripResult = document.getElementById('trip-result');
                tripResult.innerHTML = `
                    <div style="color: #dc2626">⚠️ You need to add a vehicle first to use car mode</div>
                    <a href="/createVehicle" style="display: inline-block; margin-top: 12px; padding: 8px 16px; background: #22c55e; color: white; text-decoration: none; border-radius: 8px;">
                        Add a Vehicle
                    </a>
                `;
                tripResult.style.display = 'block';
            }
            return;
        }

        // Rest of your existing vehicle loading code...
        vehicleList.innerHTML = vehicles.map(vehicle => `
            <div class="vehicle-item" data-vehicle-name="${vehicle.name}">
                <svg class="vehicle-item-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/>
                    <circle cx="7" cy="17" r="2"/>
                    <circle cx="17" cy="17" r="2"/>
                </svg>
                <span class="vehicle-item-name">${vehicle.name}</span>
                <button class="delete-button" onclick="deleteVehicle('${vehicle.name}', event)">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3 6h18"></path>
                        <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path>
                        <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
                    </svg>
                </button>
            </div>
        `).join('');

        // Add click handlers for vehicles
        document.querySelectorAll('.vehicle-item').forEach(item => {
            item.addEventListener('click', function() {
                document.querySelectorAll('.vehicle-item').forEach(i => i.classList.remove('active'));
                this.classList.add('active');
                activeVehicle = this.dataset.vehicleName;
            });
        });

        // Select first vehicle by default
        const firstVehicle = vehicleList.querySelector('.vehicle-item');
        if (firstVehicle) {
            firstVehicle.classList.add('active');
            activeVehicle = firstVehicle.dataset.vehicleName;
        }

        // Check if calculate should be enabled
        checkCalculateEnabled();
    } catch (error) {
        console.error('Error loading vehicles:', error);
    }
}

async function deleteVehicle(vehicleName, event) {
    event.stopPropagation();
    if (confirm(`Are you sure you want to delete ${vehicleName}?`)) {
        try {
            await fetch(`/api/deleteVehicle?vehicleName=${encodeURIComponent(vehicleName)}`, {
                method: 'DELETE'
            });
            loadVehicles(); // Reload the list
        } catch (error) {
            console.error('Error deleting vehicle:', error);
        }
    }
}

async function logout(event) {
    event.preventDefault();
    try {
        const response = await fetch('/api/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            window.location.href = '/';
        } else {
            console.error('Logout failed');
        }
    } catch (error) {
        console.error('Error during logout:', error);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadVehicles();
    updateProfileLink();
    initMap();
    initDraggable();
});
