document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('signupForm');
    const errorMessage = document.getElementById('errorMessage');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const passwordToggle = document.getElementById('passwordToggle');
    const confirmPasswordToggle = document.getElementById('confirmPasswordToggle');
    let passwordVisible = false;
    let confirmPasswordVisible = false;

    // Toggle password visibility functions
    function togglePasswordVisibility(inputElement, toggleButton, isVisible) {
        inputElement.type = isVisible ? 'text' : 'password';
        toggleButton.innerHTML = isVisible
            ? `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                 <path d="M2 2l20 20M6.713 6.723C3.665 8.795 2 12 2 12s4.667 6 12 6c.827 0 1.643-.075 2.432-.222M19.288 17.277C20.335 15.205 22 12 22 12s-4.667-6-12-6c-.827 0-1.643.075-2.432.222"/>
                 <path d="M14 14a2 2 0 0 1-2 2c-1.104 0-2-.896-2-2 0-1.105.896-2 2-2a2 2 0 0 1 2 2z"/>
               </svg>`
            : `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                 <path d="M12 5c-7.333 0-12 6-12 6s4.667 6 12 6 12-6 12-6-4.667-6-12-6Z"/>
                 <circle cx="12" cy="11" r="3"/>
               </svg>`;
    }

    // Password visibility toggle
    passwordToggle.addEventListener('click', function () {
        passwordVisible = !passwordVisible;
        togglePasswordVisibility(passwordInput, this, passwordVisible);
    });

    // Confirm password visibility toggle
    confirmPasswordToggle.addEventListener('click', function () {
        confirmPasswordVisible = !confirmPasswordVisible;
        togglePasswordVisibility(confirmPasswordInput, this, confirmPasswordVisible);
    });

    // Real-time password match validation
    confirmPasswordInput.addEventListener('input', function() {
        const password = passwordInput.value;
        const confirmPassword = this.value;

        if (confirmPassword && password !== confirmPassword) {
            showError('Passwords do not match');
        } else {
            errorMessage.style.display = 'none';
        }
    });

    // Form validation and submission
    form.addEventListener('submit', function (event) {
        event.preventDefault();
        errorMessage.style.display = 'none';

        const firstName = document.getElementById('firstName').value.trim();
        const lastName = document.getElementById('lastName').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = passwordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();

        // Validate passwords match
        if (password !== confirmPassword) {
            showError('Passwords do not match');
            return;
        }

        // Validate names
        if (!isValidName(firstName) || !isValidName(lastName)) {
            showError('Names must only contain letters and spaces.');
            return;
        }

        // Validate email
        if (!validateEmail(email)) {
            showError('Please enter a valid email address.');
            return;
        }

        // Validate password
        if (!isValidPassword(password)) {
            showError(
                'Password must be at least 8 characters long and include a mix of uppercase, lowercase, numbers, and special characters.'
            );
            return;
        }

        // Submit form
        const userData = { firstName, lastName, email, password };

        fetch('/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(userData),
        })
            .then((response) => {
                if (response.ok) {
                    window.location.href = '/login';
                } else {
                    showError('Error registering user. Please try again.');
                }
            })
            .catch((error) => {
                console.error('Error:', error);
                showError('An unexpected error occurred. Please try again.');
            });
    });

    function isValidName(name) {
        return /^[a-zA-Z\s]+$/.test(name);
    }

    function validateEmail(email) {
        return /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/.test(email);
    }

    function isValidPassword(password) {
        const minLength = 8;
        const hasUppercase = /[A-Z]/.test(password);
        const hasLowercase = /[a-z]/.test(password);
        const hasNumber = /\d/.test(password);
        const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);
        return (
            password.length >= minLength &&
            hasUppercase &&
            hasLowercase &&
            hasNumber &&
            hasSpecialChar
        );
    }

    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
    }
});