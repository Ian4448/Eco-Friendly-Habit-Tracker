let map;
let currentMarker;
let clickedMarker;
let currentPosition;
let startMarker, endMarker;
let selectedTransport = null;
let directionsService;
let directionsRenderer;
let activeVehicle = null;

function initMap() {
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
        const clickedPosition = mapsMouseEvent.latLng;

        // Remove previous clicked marker if it exists
        if (clickedMarker) {
            clickedMarker.setMap(null);
        }

        // Add new marker at clicked location
        clickedMarker = new google.maps.Marker({
            position: clickedPosition,
            map: map,
            icon: {
                path: google.maps.SymbolPath.CIRCLE,
                scale: 10,
                fillColor: "#F44336",
                fillOpacity: 1,
                strokeWeight: 2,
                strokeColor: "#FFFFFF"
            }
        });

        // Update clicked location text and calculate distance
        updateClickedLocationText(clickedPosition);
        calculateDistance(clickedPosition);
    });

    // Add click listeners for location inputs
    document.getElementById('start-location').addEventListener('click', () => {
        map.addListener('click', setStartLocation);
    });

    document.getElementById('end-location').addEventListener('click', () => {
        map.addListener('click', setEndLocation);
    });
}


function setStartLocation(event) {
    if (startMarker) startMarker.setMap(null);
    startMarker = new google.maps.Marker({
        position: event.latLng,
        map: map,
        label: {
            text: 'A',
            className: 'marker-label'
        }
    });
    updateLocationInput('start-location', event.latLng);
    google.maps.event.clearListeners(map, 'click');
    checkCalculateEnabled();
}

function setEndLocation(event) {
    if (endMarker) endMarker.setMap(null);
    endMarker = new google.maps.Marker({
        position: event.latLng,
        map: map,
        label: {
            text: 'B',
            className: 'marker-label'
        }
    });
    updateLocationInput('end-location', event.latLng);
    google.maps.event.clearListeners(map, 'click');
    checkCalculateEnabled();
}

function updateLocationInput(inputId, latLng) {
    fetch(`https://maps.googleapis.com/maps/api/geocode/json?latlng=${latLng.lat()},${latLng.lng()}&key=YOUR_API_KEY`)
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
    checkCalculateEnabled();
}

function checkCalculateEnabled() {
    const startInput = document.getElementById('start-location').value;
    const endInput = document.getElementById('end-location').value;
    const confirmButton = document.getElementById('confirm-trip');

    confirmButton.disabled = !(startInput && endInput && selectedTransport);
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
            displayTripResult(distance);
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

function displayTripResult(distance) {
    const tripResult = document.getElementById('trip-result');
    let transportMode = selectedTransport;
    if (selectedTransport === 'car' && activeVehicle) {
        transportMode = `${activeVehicle} (car)`;
    }
    tripResult.innerHTML = `Trip distance: ${distance}<br>Transport mode: ${transportMode}`;
    tripResult.style.display = 'block';
}

function updateLocationText() {
    if (currentPosition) {
        fetch(`https://maps.googleapis.com/maps/api/geocode/json?latlng=${currentPosition.lat},${currentPosition.lng}&key=YOUR_API_KEY`)
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

function updateClickedLocationText(position) {
    fetch(`https://maps.googleapis.com/maps/api/geocode/json?latlng=${position.lat()},${position.lng()}&key=YOUR_API_KEY`)
        .then(response => response.json())
        .then(data => {
            if (data.results && data.results[0]) {
                document.getElementById('clicked-location').innerHTML =
                    `Clicked Location: ${data.results[0].formatted_address}`;
            }
        })
        .catch(error => {
            console.error('Error fetching address:', error);
        });
}

function calculateDistance(clickedPosition) {
    if (currentPosition) {
        const start = new google.maps.LatLng(currentPosition.lat, currentPosition.lng);
        const end = clickedPosition;
        const distance = google.maps.geometry.spherical.computeDistanceBetween(start, end);

        // Convert to kilometers and format
        const distanceKm = (distance / 1000).toFixed(2);
        document.getElementById('distance').innerHTML =
            `Distance: ${distanceKm} kilometers`;
    }
}

function getCurrentUserEmail() {
    return fetch('/api/current-user')
        .then(response => response.json())
        .then(data => data.email)
        .catch(error => {
            console.error('Error fetching current user:', error);
            return null;
        });
}

async function updateProfileLink() {
    try {
        const userEmail = await getCurrentUserEmail();
        if (userEmail) {
            const encodedEmail = encodeURIComponent(userEmail);
            const profileLink = document.getElementById('profileLink');
            if (profileLink) {
                profileLink.href = `/edit/${encodedEmail}`;
            }
        }
    } catch (error) {
        console.error('Error updating profile link:', error);
    }
}
async function loadVehicles() {
    try {
        const response = await fetch('/getVehicles');
        const vehicles = await response.json();
        const vehicleList = document.getElementById('vehicleList');

        if (!vehicles || vehicles.length === 0) {
            vehicleList.innerHTML = `
                    <div class="empty-state">
                        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/>
                            <circle cx="7" cy="17" r="2"/>
                            <circle cx="17" cy="17" r="2"/>
                        </svg>
                        <p>No vehicles found</p>
                        <a href="/createVehicle">Add Your First Vehicle</a>
                    </div>
                `;
            return;
        }

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

        // Add click handlers
        document.querySelectorAll('.vehicle-item').forEach(item => {
            item.addEventListener('click', function() {
                document.querySelectorAll('.vehicle-item').forEach(i => i.classList.remove('active'));
                this.classList.add('active');
                activeVehicle = this.dataset.vehicleName;
                if (selectedTransport === 'car') {
                    checkCalculateEnabled();
                }
            });
        });
    } catch (error) {
        console.error('Error loading vehicles:', error);
    }
}

async function deleteVehicle(vehicleName, event) {
    event.stopPropagation();
    if (confirm(`Are you sure you want to delete ${vehicleName}?`)) {
        try {
            await fetch(`/deleteVehicle?vehicleName=${encodeURIComponent(vehicleName)}`, {
                method: 'DELETE'
            });
            loadVehicles(); // Reload the list
        } catch (error) {
            console.error('Error deleting vehicle:', error);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadVehicles();
    updateProfileLink();
    initMap();
});
