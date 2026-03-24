// adminDashboard.js

import { openModal } from "./components/modals.js";
import {
  getDoctors,
  filterDoctors,
  saveDoctor,
} from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

function getElement(id) {
  return document.getElementById(id);
}

function getValue(id) {
  const el = getElement(id);
  return el ? el.value.trim() : "";
}

function getCheckedValues(selector) {
  return Array.from(document.querySelectorAll(selector))
    .filter((checkbox) => checkbox.checked)
    .map((checkbox) => checkbox.value);
}

function closeAddDoctorModal() {
  if (typeof window.closeModal === "function") {
    window.closeModal();
    return;
  }

  const modal = document.getElementById("modal");
  if (modal) {
    modal.classList.remove("show");
    modal.style.display = "none";
    modal.setAttribute("aria-hidden", "true");
  }

  const modalBody = document.getElementById("modal-body");
  if (modalBody) {
    modalBody.innerHTML = "";
  }
}

function bindAddDoctorButton() {
  const addDocBtn = document.getElementById("addDocBtn");
  if (!addDocBtn) return;

  if (addDocBtn.dataset.bound === "true") return;

  addDocBtn.addEventListener("click", () => {
    openModal("addDoctor");
  });

  addDocBtn.dataset.bound = "true";
}

function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  contentDiv.innerHTML = "";

  if (!Array.isArray(doctors) || doctors.length === 0) {
    contentDiv.innerHTML = `<p class="no-doctors-message">No doctors found with the given filters.</p>`;
    return;
  }

  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

async function loadDoctorCards() {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  try {
    const doctors = await getDoctors();
    contentDiv.innerHTML = "";
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Error loading doctor cards:", error);
    contentDiv.innerHTML = `<p class="no-doctors-message">Unable to load doctors at the moment.</p>`;
  }
}

async function filterDoctorsOnChange() {
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  const name = searchBar && searchBar.value.trim() !== "" ? searchBar.value.trim() : null;
  const time = filterTime && filterTime.value.trim() !== "" ? filterTime.value.trim() : null;
  const specialty =
    filterSpecialty && filterSpecialty.value.trim() !== ""
      ? filterSpecialty.value.trim()
      : null;

  try {
    const doctors = await filterDoctors(name, time, specialty);
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("Something went wrong while filtering doctors.");
  }
}

async function adminAddDoctor(event) {
  if (event) event.preventDefault();

  const name = getValue("doctorName");
  const email = getValue("doctorEmail");
  const mobile = getValue("doctorMobile");
  const password = getValue("doctorPassword");
  const specialty = getValue("doctorSpecialty");
  const availability = getCheckedValues('input[name="availability"]:checked');

  const token = localStorage.getItem("token");

  if (!token) {
    alert("Admin session expired. Please log in again.");
    window.location.href = "/";
    return;
  }

  if (!name || !email || !mobile || !password || !specialty) {
    alert("Please fill in all required doctor fields.");
    return;
  }

  const doctor = {
    name,
    email,
    mobile,
    password,
    specialization: specialty,
    availability,
  };

  try {
    const result = await saveDoctor(doctor, token);

    if (result.success) {
      alert(result.message || "Doctor added successfully.");
      closeAddDoctorModal();
      await loadDoctorCards();
    } else {
      alert(result.message || "Failed to add doctor.");
    }
  } catch (error) {
    console.error("Error adding doctor:", error);
    alert("Something went wrong while adding the doctor.");
  }
}

function bindFilterEvents() {
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  if (searchBar) {
    searchBar.addEventListener("input", filterDoctorsOnChange);
  }

  if (filterTime) {
    filterTime.addEventListener("change", filterDoctorsOnChange);
  }

  if (filterSpecialty) {
    filterSpecialty.addEventListener("change", filterDoctorsOnChange);
  }
}

window.adminAddDoctor = adminAddDoctor;
window.loadDoctorCards = loadDoctorCards;
window.filterDoctorsOnChange = filterDoctorsOnChange;
window.renderDoctorCards = renderDoctorCards;

document.addEventListener("DOMContentLoaded", async () => {
  bindAddDoctorButton();
  bindFilterEvents();
  await loadDoctorCards();

  const observer = new MutationObserver(() => {
    bindAddDoctorButton();
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true,
  });
});