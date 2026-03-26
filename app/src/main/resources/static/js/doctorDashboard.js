// doctorDashboard.js

import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

const appointmentTableBody = document.getElementById("patientTableBody");

function getTodayDate() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

let selectedDate = getTodayDate();
let token = localStorage.getItem("token");
let patientName = "null";

function createMessageRow(message) {
  const row = document.createElement("tr");

  const cell = document.createElement("td");
  cell.colSpan = 5;
  cell.textContent = message;
  cell.classList.add("noPatientRecord");

  row.appendChild(cell);
  return row;
}

async function loadAppointments() {
  if (!appointmentTableBody) return;

  try {
    appointmentTableBody.innerHTML = "";

    const appointments = await getAllAppointments(
      selectedDate,
      patientName,
      token
    );

    const appointmentList = Array.isArray(appointments)
      ? appointments
      : Array.isArray(appointments?.appointments)
        ? appointments.appointments
        : [];

    if (appointmentList.length === 0) {
      appointmentTableBody.appendChild(
        createMessageRow("No Appointments found for today.")
      );
      return;
    }

    appointmentList.forEach((appointment) => {
      const patient = {
        id:
          appointment.patient?.id ||
          appointment.patientId ||
          appointment.id ||
          "N/A",

        name:
          appointment.patient?.name ||
          appointment.patientName ||
          appointment.name ||
          "Unknown",

        phone:
          appointment.patient?.phone ||
          appointment.patient?.mobile ||
          appointment.phone ||
          appointment.mobile ||
          "N/A",

        email:
          appointment.patient?.email ||
          appointment.email ||
          "N/A",
      };
      console.log(patient);
      console.log(appointment.patient.phone); // coś nie odczytuje telefonu, ani maila

      const appointmentId =
        appointment.id || appointment.appointmentId || "N/A";

      const doctorId =
        appointment.doctor?.id || appointment.doctorId || "N/A";

      const row = createPatientRow(patient, appointmentId, doctorId);
      appointmentTableBody.appendChild(row);
    });
  } catch (error) {
    console.error("Error loading appointments:", error);
    appointmentTableBody.innerHTML = "";
    appointmentTableBody.appendChild(
      createMessageRow("Error loading appointments. Try again later.")
    );
  }
}

function bindSearchBar() {
  const searchBar = document.getElementById("searchBar");
  if (!searchBar) return;

  searchBar.addEventListener("input", () => {
    const value = searchBar.value.trim();
    patientName = value !== "" ? value : "null";
    loadAppointments();
  });
}

function bindTodayButton() {
  const todayButton = document.getElementById("todayButton");
  const datePicker = document.getElementById("datePicker");

  if (!todayButton) return;

  todayButton.addEventListener("click", () => {
    selectedDate = getTodayDate();

    if (datePicker) {
      datePicker.value = selectedDate;
    }

    loadAppointments();
  });
}

function bindDatePicker() {
  const datePicker = document.getElementById("datePicker");
  if (!datePicker) return;

  datePicker.value = selectedDate;

  datePicker.addEventListener("change", () => {
    selectedDate = datePicker.value || getTodayDate();
    loadAppointments();
  });
}

document.addEventListener("DOMContentLoaded", () => {
  token = localStorage.getItem("token");

  if (typeof window.renderContent === "function") {
    window.renderContent();
  }

  bindSearchBar();
  bindTodayButton();
  bindDatePicker();
  loadAppointments();
});

window.loadAppointments = loadAppointments;
