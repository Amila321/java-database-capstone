// index.js

import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = API_BASE_URL + "/admin";
const DOCTOR_API = API_BASE_URL + "/doctor/login";

function getElementByPossibleIds(ids) {
  for (const id of ids) {
    const element = document.getElementById(id);
    if (element) return element;
  }
  return null;
}

function getInputValue(possibleIds) {
  const input = getElementByPossibleIds(possibleIds);
  return input ? input.value.trim() : "";
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

function handleSuccessfulLogin(role, token) {
  localStorage.setItem("token", token);

  if (typeof window.selectRole === "function") {
    window.selectRole(role);
  } else {
    localStorage.setItem("userRole", role);

    if (role === "admin") {
      window.location.href = "/admin/adminDashboard";
    } else if (role === "doctor") {
      window.location.href = "/doctor/doctorDashboard";
    } else {
      window.location.href = "/";
    }
  }
}

window.onload = function () {
  const adminBtn =
    document.getElementById("adminLogin") ||
    document.getElementById("adminBtn");

  const doctorBtn =
    document.getElementById("doctorLogin") ||
    document.getElementById("doctorBtn");

  if (adminBtn) {
    adminBtn.addEventListener("click", () => {
      openModal("adminLogin");
    });
  }

  if (doctorBtn) {
    doctorBtn.addEventListener("click", () => {
      openModal("doctorLogin");
    });
  }
};

window.adminLoginHandler = async function adminLoginHandler(event) {
  if (event) event.preventDefault();

  const username = getInputValue(["adminUsername", "username"]);
  const password = getInputValue(["adminPassword", "password"]);

  if (!username || !password) {
    alert("Please enter username and password.");
    return;
  }

  const admin = { username, password };

  try {
    const response = await fetch(ADMIN_API, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(admin),
    });

    if (!response.ok) {
      alert("Invalid credentials!");
      return;
    }

    const data = await response.json();
    const token = extractToken(data);

    if (!token) {
      alert("Login succeeded, but no token was returned.");
      return;
    }

    handleSuccessfulLogin("admin", token);
  } catch (error) {
    console.error("Admin login error:", error);
    alert("Something went wrong during admin login.");
  }
};

window.doctorLoginHandler = async function doctorLoginHandler(event) {
  if (event) event.preventDefault();

  const email = getInputValue(["doctorEmail", "email"]);
  const password = getInputValue(["doctorPassword", "password"]);

  if (!email || !password) {
    alert("Please enter email and password.");
    return;
  }

  const doctor = { email, password };

  try {
    const response = await fetch(DOCTOR_API, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(doctor),
    });

    if (!response.ok) {
      alert("Invalid credentials!");
      return;
    }

    const data = await response.json();
    const token = extractToken(data);

    if (!token) {
      alert("Login succeeded, but no token was returned.");
      return;
    }

    handleSuccessfulLogin("doctor", token);
  } catch (error) {
    console.error("Doctor login error:", error);
    alert("Something went wrong during doctor login.");
  }
};