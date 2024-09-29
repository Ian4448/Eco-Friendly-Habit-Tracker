// VehicleAddPage.js

document.addEventListener("DOMContentLoaded", function () {
    const vehicleForm = document.getElementById("vehicleForm");

    vehicleForm.addEventListener("submit", function (e) {
        e.preventDefault(); // Prevent form submission for validation

        const make = document.getElementById("make").value.trim();
        const model = document.getElementById("model").value.trim();
        const fuelEfficiency = document.getElementById("fuelEfficiency").value.trim();

        if (make === "" || model === "" || fuelEfficiency === "") {
            alert("Please fill in all fields.");
            return;
        }

        if (isNaN(fuelEfficiency) || fuelEfficiency <= 0) {
            alert("Please enter a valid number for fuel efficiency (greater than 0).");
            return;
        }

        // If validation passes, proceed with form submission or AJAX call
        alert("Vehicle information form is valid and ready for submission!");
    });
});