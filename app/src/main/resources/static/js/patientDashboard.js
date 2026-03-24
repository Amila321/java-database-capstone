// patientDashboard.js

import { createDoctorCard } from "./components/doctorCard.js";
import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors } from "./services/doctorServices.js";
import { patientLogin, patientSignup } from "./services/patientServices.js";

const contentDiv = document.getElementById("content");

function getElement(id) {
  return document.getElementById(id);
}

function getValue(id) {
  const el = getElement(id);
  return el ? el.value.trim() : "";
}

function closeAnyModal() {
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

function renderDoctorCards(doctors) {
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
  if (!contentDiv) return;

  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Error loading doctors:", error);
    contentDiv.innerHTML = `<p class="no-doctors-message">Unable to load doctors at the moment.</p>`;
  }
}

async function filterDoctorsOnChange() {
  const searchBar = getElement("searchBar");
  const filterTime = getElement("filterTime");
  const filterSpecialty = getElement("filterSpecialty");

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
    if (contentDiv) {
      contentDiv.innerHTML = `<p class="no-doctors-message">No doctors found with the given filters.</p>`;
    }
  }
}

function bindFilterEvents() {
  const searchBar = getElement("searchBar");
  const filterTime = getElement("filterTime");
  const filterSpecialty = getElement("filterSpecialty");

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

function bindAuthButtons() {
  const signupBtn = getElement("patientSignup");
  const loginBtn = getElement("patientLogin");

  if (signupBtn && signupBtn.dataset.bound !== "true") {
    signupBtn.addEventListener("click", () => openModal("patientSignup"));
    signupBtn.dataset.bound = "true";
  }

  if (loginBtn && loginBtn.dataset.bound !== "true") {
    loginBtn.addEventListener("click", () => openModal("patientLogin"));
    loginBtn.dataset.bound = "true";
  }
}

function extractToken(data) {
  return (
    data?.token ||
    data?.accessToken ||
    data?.jwt ||
    data?.data?.token ||
    null
  );
}

window.signupPatient = async function signupPatient(event) {
  if (event) event.preventDefault();

  const name = getValue("patientName");
  const email = getValue("patientEmail");
  const password = getValue("patientPassword");
  const phone = getValue("patientPhone");
  const address = getValue("patientAddress");

  if (!name || !email || !password || !phone || !address) {
    alert("Please fill in all patient signup fields.");
    return;
  }

  const data = {
    name,
    email,
    password,
    phone,
    address,
  };

  try {
    const result = await patientSignup(data);

    if (result && result.success) {
      alert(result.message || "Signup successful.");
      closeAnyModal();
      await loadDoctorCards();
    } else {
      alert(result?.message || "Signup failed.");
    }
  } catch (error) {
    console.error("Signup error:", error);
    alert("Something went wrong during signup.");
  }
};

window.loginPatient = async function loginPatient(event) {
  if (event) event.preventDefault();

  const email = getValue("loginEmail") || getValue("patientLoginEmail");
  const password = getValue("loginPassword") || getValue("patientLoginPassword");

  if (!email || !password) {
    alert("Please enter email and password.");
    return;
  }

  const data = { email, password };

  try {
    const response = await patientLogin(data);

    if (!response) {
      alert("Unable to connect to the server.");
      return;
    }

    if (!response.ok) {
      alert("Invalid credentials!");
      return;
    }

    const result = await response.json();
    const token = extractToken(result);

    if (!token) {
      alert("Login succeeded, but no token was returned.");
      return;
    }

    localStorage.setItem("token", token);
    localStorage.setItem("userRole", "loggedPatient");

    window.location.href = "/pages/loggedPatientDashboard.html";
  } catch (error) {
    console.error("Login error:", error);
    alert("Something went wrong during login.");
  }
};

window.loadDoctorCards = loadDoctorCards;
window.renderDoctorCards = renderDoctorCards;
window.filterDoctorsOnChange = filterDoctorsOnChange;

document.addEventListener("DOMContentLoaded", async () => {
  bindAuthButtons();
  bindFilterEvents();
  await loadDoctorCards();

  const observer = new MutationObserver(() => {
    bindAuthButtons();
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true,
  });
});