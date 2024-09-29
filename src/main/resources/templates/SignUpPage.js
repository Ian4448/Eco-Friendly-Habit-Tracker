// SignUpPage.js

// Wait for the DOM to fully load before running the script
document.addEventListener("DOMContentLoaded", function () {
    const signUpForm = document.getElementById("signUpForm");

    signUpForm.addEventListener("submit", function (e) {
        e.preventDefault(); // Prevent form submission for validation

        const firstName = document.getElementById("firstName").value.trim();
        const lastName = document.getElementById("lastName").value.trim();
        const email = document.getElementById("email").value.trim();
        const city = document.getElementById("city").value.trim();
        const state = document.getElementById("state").value.trim();
        const country = document.getElementById("country").value.trim();

        if (firstName === "" || lastName === "" || email === "" || city === "" || state === "" || country === "") {
            alert("Please fill in all fields.");
            return;
        }

        // Optionally, perform additional email format validation here
        if (!validateEmail(email)) {
            alert("Please enter a valid email address.");
            return;
        }

        // If validation passes, proceed with form submission or AJAX call
        alert("Sign-up form is valid and ready for submission!");
    });

    function validateEmail(email) {
        const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
        return emailPattern.test(email);
    }
});