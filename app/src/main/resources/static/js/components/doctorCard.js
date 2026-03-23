// doctorCard.js

import { showBookingOverlay } from "../services/loggedPatient.js";
import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";

export function createDoctorCard(doctor) {
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    const role = localStorage.getItem("userRole");

    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    const name = document.createElement("h3");
    name.textContent = doctor.name || "Unknown Doctor";

    const specialization = document.createElement("p");
    specialization.innerHTML = `<strong>Specialty:</strong> ${
        doctor.specialization || doctor.specialty || "Not provided"
    }`;

    const email = document.createElement("p");
    email.innerHTML = `<strong>Email:</strong> ${doctor.email || "Not provided"}`;

    const availability = document.createElement("p");
    const availableSlots = Array.isArray(doctor.availability)
        ? doctor.availability.join(", ")
        : doctor.availability || "Not available";

    availability.innerHTML = `<strong>Availability:</strong> ${availableSlots}`;

    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(availability);

    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    if (role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";
        removeBtn.classList.add("card-btn", "delete-btn");

        removeBtn.addEventListener("click", async () => {
            const confirmed = window.confirm(
                `Are you sure you want to delete Dr. ${doctor.name}?`
            );

            if (!confirmed) return;

            const token = localStorage.getItem("token");

            if (!token) {
                alert("Session expired. Please log in again.");
                localStorage.removeItem("userRole");
                window.location.href = "/";
                return;
            }

            try {
                const doctorId = doctor.id || doctor.doctorId;

                if (!doctorId) {
                    alert("Doctor ID is missing. Cannot delete this doctor.");
                    return;
                }

                await deleteDoctor(doctorId, token);
                alert("Doctor deleted successfully.");
                card.remove();
            } catch (error) {
                console.error("Error deleting doctor:", error);
                alert("Failed to delete doctor. Please try again.");
            }
        });

        actionsDiv.appendChild(removeBtn);
    } else if (role === "patient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.classList.add("card-btn", "book-btn");

        bookNow.addEventListener("click", () => {
            alert("Patient needs to login first.");
        });

        actionsDiv.appendChild(bookNow);
    } else if (role === "loggedPatient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.classList.add("card-btn", "book-btn");

        bookNow.addEventListener("click", async (e) => {
            const token = localStorage.getItem("token");

            if (!token) {
                alert("Session expired. Please log in again.");
                localStorage.setItem("userRole", "patient");
                window.location.href = "/";
                return;
            }

            try {
                const patientData = await getPatientData(token);
                showBookingOverlay(e, doctor, patientData);
            } catch (error) {
                console.error("Error fetching patient data:", error);
                alert("Unable to start booking. Please try again.");
            }
        });

        actionsDiv.appendChild(bookNow);
    }

    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    return card;
}