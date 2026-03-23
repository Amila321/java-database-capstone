// header.js

function renderHeader() {
    const headerDiv = document.getElementById("header");
    if (!headerDiv) return;

    if (window.location.pathname.endsWith("/")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");

        headerDiv.innerHTML = `
      <header class="header">
        <div class="logo-section">
          <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
          <span class="logo-title">Hospital CMS</span>
        </div>
      </header>
    `;
        return;
    }

    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "/";
        return;
    }

    let headerContent = `
    <header class="header">
      <div class="logo-section">
        <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
        <span class="logo-title">Hospital CMS</span>
      </div>
      <nav class="header-nav">
  `;

    if (role === "admin") {
        headerContent += `
      <button id="addDocBtn" class="adminBtn">Add Doctor</button>
      <a href="#" id="logoutLink">Logout</a>
    `;
    } else if (role === "doctor") {
        headerContent += `
      <button id="doctorHomeBtn" class="adminBtn">Home</button>
      <a href="#" id="logoutLink">Logout</a>
    `;
    } else if (role === "patient") {
        headerContent += `
      <button id="patientLogin" class="adminBtn">Login</button>
      <button id="patientSignup" class="adminBtn">Sign Up</button>
    `;
    } else if (role === "loggedPatient") {
        headerContent += `
      <button id="home" class="adminBtn">Home</button>
      <button id="patientAppointments" class="adminBtn">Appointments</button>
      <a href="#" id="logoutPatientLink">Logout</a>
    `;
    }

    headerContent += `
      </nav>
    </header>
  `;

    headerDiv.innerHTML = headerContent;
    attachHeaderButtonListeners();
}

function attachHeaderButtonListeners() {
    const addDocBtn = document.getElementById("addDocBtn");
    const logoutLink = document.getElementById("logoutLink");
    const logoutPatientLink = document.getElementById("logoutPatientLink");
    const doctorHomeBtn = document.getElementById("doctorHomeBtn");
    const patientLogin = document.getElementById("patientLogin");
    const patientSignup = document.getElementById("patientSignup");
    const homeBtn = document.getElementById("home");
    const patientAppointments = document.getElementById("patientAppointments");

    if (addDocBtn) {
        addDocBtn.addEventListener("click", () => {
            if (typeof openModal === "function") {
                openModal("addDoctor");
            } else {
                alert("Add Doctor modal is not available.");
            }
        });
    }

    if (logoutLink) {
        logoutLink.addEventListener("click", (e) => {
            e.preventDefault();
            logout();
        });
    }

    if (logoutPatientLink) {
        logoutPatientLink.addEventListener("click", (e) => {
            e.preventDefault();
            logoutPatient();
        });
    }

    if (doctorHomeBtn) {
        doctorHomeBtn.addEventListener("click", () => {
            if (typeof selectRole === "function") {
                selectRole("doctor");
            } else {
                window.location.href = "/doctor/doctorDashboard";
            }
        });
    }

    if (patientLogin) {
        patientLogin.addEventListener("click", () => {
            if (typeof openModal === "function") {
                openModal("login");
            } else {
                alert("Login modal is not available.");
            }
        });
    }

    if (patientSignup) {
        patientSignup.addEventListener("click", () => {
            if (typeof openModal === "function") {
                openModal("signup");
            } else {
                alert("Signup modal is not available.");
            }
        });
    }

    if (homeBtn) {
        homeBtn.addEventListener("click", () => {
            window.location.href = "/pages/loggedPatientDashboard.html";
        });
    }

    if (patientAppointments) {
        patientAppointments.addEventListener("click", () => {
            window.location.href = "/pages/patientAppointments.html";
        });
    }
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "/";
}

function logoutPatient() {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient");
    window.location.href = "/pages/patientDashboard.html";
}

window.renderHeader = renderHeader;
window.attachHeaderButtonListeners = attachHeaderButtonListeners;
window.logout = logout;
window.logoutPatient = logoutPatient;

document.addEventListener("DOMContentLoaded", renderHeader);