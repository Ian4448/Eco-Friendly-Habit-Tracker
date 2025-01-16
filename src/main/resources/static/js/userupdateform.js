function getCurrentUserId() {
    const pathSegments = window.location.pathname.split('/');
    return pathSegments[pathSegments.length - 1];
}

function showSuccessMessage(message) {
    const successMessage = document.getElementById('successMessage');
    successMessage.textContent = message;
    successMessage.style.display = 'block';
    setTimeout(() => {
        successMessage.style.display = 'none';
    }, 3000);
}

function showError(message, elementId) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    input.type = input.type === 'password' ? 'text' : 'password';
}

document.getElementById('settingsForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    document.querySelectorAll('.error').forEach(error => error.style.display = 'none');

    const formData = new FormData(e.target);
    const userId = getCurrentUserId();

    // Only include password fields if they're not empty
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');

    // Basic validation
    if (password !== confirmPassword) {
        showError('Passwords do not match', 'passwordError');
        return;
    }

    const userData = {
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        email: formData.get('email'),
        id: userId
    };

    // Only add password to request if it's being changed
    if (password) {
        userData.password = password;
    }

    try {
        const response = await fetch('/api/updateUser', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            const changes = [];
            if (password) changes.push('password');
            if (
                userData.firstName !== document.getElementById('firstName').defaultValue ||
                userData.lastName !== document.getElementById('lastName').defaultValue ||
                userData.email !== document.getElementById('email').defaultValue
            ) {
                changes.push('profile information');
            }

            let successMsg = 'Settings updated successfully';
            if (changes.length > 0) {
                successMsg = `Updated: ${changes.join(', ')}`;
            }
            showSuccessMessage(successMsg);

            // No need to redirect since we're using IDs which don't change
        } else {
            const errorData = await response.json();
            showError(errorData.message || 'Error updating user information', 'generalError');
        }
    } catch (error) {
        console.error('Error updating settings:', error);
        showError('An unexpected error occurred', 'generalError');
    }
});

async function loadUserData() {
    const form = document.getElementById('settingsForm');
    form.classList.add('loading');

    try {
        const userId = getCurrentUserId();
        if (!userId) {
            throw new Error('No user ID found in URL');
        }

        const userResponse = await fetch(`/api/user/${userId}`);
        if (!userResponse.ok) {
            throw new Error('Failed to fetch user data');
        }

        const userData = await userResponse.json();

        // Pre-fill the form
        document.getElementById('firstName').value = userData.firstName || '';
        document.getElementById('lastName').value = userData.lastName || '';
        document.getElementById('email').value = userData.email || '';

        // Set these as default values for change detection
        document.getElementById('firstName').defaultValue = userData.firstName || '';
        document.getElementById('lastName').defaultValue = userData.lastName || '';
        document.getElementById('email').defaultValue = userData.email || '';
    } catch (error) {
        console.error('Error loading user data:', error);
        showError('Error loading user data. Please try again.', 'generalError');
    } finally {
        form.classList.remove('loading');
    }
}

window.addEventListener('load', loadUserData);