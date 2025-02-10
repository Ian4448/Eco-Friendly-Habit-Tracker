function getEcoStatus(mpg) {
    const mpgNum = parseFloat(mpg);
    if (mpgNum < 25) {
        return {
            class: 'bg-gradient-to-r from-red-500 to-red-600 text-white',
            label: 'Low'
        };
    } else if (mpgNum < 40) {
        return {
            class: 'bg-gradient-to-r from-yellow-500 to-yellow-600 text-white',
            label: 'Medium'
        };
    } else {
        return {
            class: 'bg-gradient-to-r from-green-400 to-green-500 text-white',
            label: 'High'
        };
    }
}

function showError(message) {
    const errorDiv = document.getElementById('errorMessage');
    const successDiv = document.getElementById('successMessage');
    errorDiv.textContent = message;
    errorDiv.classList.remove('hidden');
    successDiv.classList.add('hidden');
    errorDiv.classList.add('slide-in');
}

function showSuccess(message) {
    const errorDiv = document.getElementById('errorMessage');
    const successDiv = document.getElementById('successMessage');
    successDiv.textContent = message;
    successDiv.classList.remove('hidden');
    errorDiv.classList.add('hidden');
    successDiv.classList.add('slide-in');
}

function clearMessages() {
    document.getElementById('errorMessage').classList.add('hidden');
    document.getElementById('successMessage').classList.add('hidden');
}

// Form submission handler
document.getElementById("vehicleForm").onsubmit = function (event) {
    event.preventDefault();
    clearMessages();

    const formData = new FormData(this);
    const vehicleData = Object.fromEntries(formData);

    fetch('/api/addVehicle', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(vehicleData)
    })
        .then(async (response) => {
            const data = await response.json();

            if (!response.ok) {
                switch (response.status) {
                    case 409:
                        showError(data.message || "A vehicle with this name already exists.");
                        break;
                    case 401:
                        showError("You must be logged in to add a vehicle.");
                        break;
                    case 500:
                        showError(data.message || "An internal server error occurred. Please try again later.");
                        break;
                    default:
                        showError("An unexpected error occurred. Please try again.");
                }
                throw new Error(data.message || 'Error adding vehicle');
            }

            return data;
        })
        .then((data) => {
            showSuccess("Vehicle added successfully!");
            addVehicleToList(data);
            this.reset();
        })
        .catch((error) => {
            console.error('Error:', error);
        });
};

function deleteVehicle(vehicleName) {
    if (confirm('Are you sure you want to delete this vehicle?')) {
        fetch(`/api/deleteVehicle?vehicleName=${encodeURIComponent(vehicleName)}`, {
            method: 'DELETE'
        })
            .then((response) => {
                if (response.ok) {
                    const element = document.querySelector(`[data-vehicle-name="${vehicleName}"]`);
                    element.style.opacity = '0';
                    element.style.transform = 'translateY(10px)';
                    setTimeout(() => {
                        element.remove();
                        if (document.querySelectorAll('.vehicle-item').length === 0) {
                            document.getElementById('noVehiclesMessage').classList.remove('hidden');
                        }
                    }, 300);
                } else {
                    showError("Error deleting vehicle.");
                }
            })
            .catch((error) => {
                console.error('Error:', error);
                showError("Error deleting vehicle.");
            });
    }
}

async function fetchUserName() {
    try {
        // Get user ID from cookie
        const userId = document.cookie
            .split('; ')
            .find(row => row.startsWith('user_id='))
            ?.split('=')[1];

        if (!userId) {
            console.warn('User ID not found');
            updateUserDisplay('Guest');
            return;
        }

        const profileLink = document.getElementById('profileLink');
        if (profileLink) {
            profileLink.href = `/edit/${userId}`;
        }

        const response = await fetch(`/api/user/${userId}`);
        if (!response.ok) {
            throw new Error('Failed to fetch user data');
        }

        const userData = await response.json();
        const displayName = userData.firstName
            ? `${userData.firstName} ${userData.lastName || ''}`.trim()
            : 'Guest';
        updateUserDisplay(displayName);
    } catch (error) {
        console.error('Error fetching user data:', error);
        updateUserDisplay('Guest');
    }
}

function updateUserDisplay(displayName) {
    const usernameElement = document.getElementById('username');
    if (usernameElement) {
        usernameElement.style.transition = 'opacity 0.3s ease';
        usernameElement.style.opacity = '0';
        setTimeout(() => {
            usernameElement.textContent = displayName;
            usernameElement.style.opacity = '1';
        }, 50);
    }
}

function fetchVehicles() {
    fetch('/api/getVehicles')
        .then((response) => response.json())
        .then((vehicles) => {
            const vehicleList = document.getElementById('vehicleList');
            const noVehiclesMessage = document.getElementById('noVehiclesMessage');

            vehicleList.querySelectorAll('.vehicle-item').forEach((vehicle) => vehicle.remove());

            if (vehicles.length === 0) {
                noVehiclesMessage.classList.remove('hidden');
            } else {
                noVehiclesMessage.classList.add('hidden');
                vehicles.forEach((vehicle, index) => {
                    setTimeout(() => {
                        addVehicleToList(vehicle);
                    }, index * 100);
                });
            }
        })
        .catch((error) => console.error('Error fetching vehicles:', error));
}

function addVehicleToList(vehicle) {
    const vehicleList = document.getElementById('vehicleList');
    const noVehiclesMessage = document.getElementById('noVehiclesMessage');

    if (!noVehiclesMessage.classList.contains('hidden')) {
        noVehiclesMessage.classList.add('hidden');
    }

    const vehicleItem = document.createElement('div');
    vehicleItem.classList.add(
        'vehicle-item',
        'vehicle-card',
        'glass-card',
        'rounded-xl',
        'p-6',
        'slide-in'
    );
    vehicleItem.setAttribute('data-vehicle-name', vehicle.name);

    const ecoStatus = getEcoStatus(vehicle.mpg);

    vehicleItem.innerHTML = `
        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <div class="grid grid-cols-2 gap-x-8 gap-y-3 flex-grow">
                <p class="text-sm text-gray-600">
                    <span class="font-medium text-gray-800">Name:</span> ${vehicle.name}
                </p>
                <p class="text-sm text-gray-600">
                    <span class="font-medium text-gray-800">Make:</span> ${vehicle.make}
                </p>
                <p class="text-sm text-gray-600">
                    <span class="font-medium text-gray-800">Model:</span> ${vehicle.model}
                </p>
                <p class="text-sm text-gray-600">
                    <span class="font-medium text-gray-800">MPG:</span> ${vehicle.mpg}
                    <span class="efficiency-badge ${ecoStatus.class}">${ecoStatus.label}</span>
                </p>
            </div>
            <button type="button" 
                    class="delete-button min-w-[90px] px-4 py-2 rounded-lg text-sm font-medium text-red-600 flex items-center justify-center gap-2 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-opacity-50">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
                Delete
            </button>
        </div>
    `;

    // Add click event listener to delete button after creating the element
    const deleteButton = vehicleItem.querySelector('.delete-button');
    deleteButton.addEventListener('click', () => deleteVehicle(vehicle.name));

    vehicleList.appendChild(vehicleItem);
}

// Initialize everything when the page loads
document.addEventListener('DOMContentLoaded', () => {
    fetchVehicles();
    fetchUserName();

    // Refresh username periodically
    setInterval(fetchUserName, 300000); // every 5 minutes
});
