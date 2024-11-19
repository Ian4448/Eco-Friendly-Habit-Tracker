document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('signinForm');
    const clientErrorMessage = document.getElementById('clientErrorMessage');
    const serverErrorMessage = document.getElementById('serverErrorMessage');
    const passwordInput = document.getElementById('password');
    const passwordToggle = document.getElementById('passwordToggle');
    let passwordVisible = false;

    // Show server error if exists
    if (serverErrorMessage && serverErrorMessage.textContent.trim() !== '') {
        serverErrorMessage.style.display = 'block';
    }

    // Form validation
    form.addEventListener('submit', function (event) {
        clientErrorMessage.style.display = 'none';
        const username = document.getElementById('username').value;
        const password = passwordInput.value;

        if (!username || !password) {
            event.preventDefault();
            clientErrorMessage.style.display = 'block';
        }
    });

    // Password visibility toggle with icon update
    passwordToggle.addEventListener('click', function () {
        passwordVisible = !passwordVisible;
        passwordInput.type = passwordVisible ? 'text' : 'password';

        // Update icon based on visibility
        this.innerHTML = passwordVisible
            ? `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                   <path d="M2 2l20 20M6.713 6.723C3.665 8.795 2 12 2 12s4.667 6 12 6c.827 0 1.643-.075 2.432-.222M19.288 17.277C20.335 15.205 22 12 22 12s-4.667-6-12-6c-.827 0-1.643.075-2.432.222"/>
                   <path d="M14 14a2 2 0 0 1-2 2c-1.104 0-2-.896-2-2 0-1.105.896-2 2-2a2 2 0 0 1 2 2z"/>
               </svg>`
            : `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                   <path d="M12 5c-7.333 0-12 6-12 6s4.667 6 12 6 12-6 12-6-4.667-6-12-6Z"/>
                   <circle cx="12" cy="11" r="3"/>
               </svg>`;
    });
});
