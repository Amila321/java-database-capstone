// patientServices.js

import { API_BASE_URL } from "../config/config.js";

const PATIENT_API = API_BASE_URL + "/patient";

async function parseJsonSafe(response) {
  try {
    return await response.json();
  } catch (error) {
    return null;
  }
}

/*
  Function: patientSignup
  Purpose: Register a new patient
*/
export async function patientSignup(data) {
  try {
    const response = await fetch(`${PATIENT_API}/signup`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });

    const result = await parseJsonSafe(response);

    return {
      success: response.ok,
      message:
        result?.message ||
        (response.ok
          ? "Patient registered successfully."
          : "Failed to register patient."),
    };
  } catch (error) {
    console.error("Error during patient signup:", error);
    return {
      success: false,
      message: "Something went wrong during signup.",
    };
  }
}

/*
  Function: patientLogin
  Purpose: Authenticate patient
  Note: returns full fetch response so frontend can inspect status/token
*/
export async function patientLogin(data) {
  try {
    console.log("Patient login payload:", data);

    const response = await fetch(`${PATIENT_API}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });

    return response;
  } catch (error) {
    console.error("Error during patient login:", error);
    return null;
  }
}

/*
  Function: getPatientData
  Purpose: Fetch logged-in patient details using token
*/
export async function getPatientData(token) {
  try {
    if (!token) {
      console.error("Token is missing.");
      return null;
    }

    const response = await fetch(
      `${PATIENT_API}/${encodeURIComponent(token)}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      console.error("Failed to fetch patient data.");
      return null;
    }

    const result = await parseJsonSafe(response);

    return result?.patient || result || null;
  } catch (error) {
    console.error("Error fetching patient data:", error);
    return null;
  }
}

/*
  Function: getPatientAppointments
  Purpose: Fetch appointments for patient or doctor dashboard
*/
export async function getPatientAppointments(id, token, user) {
  try {
    if (!id || !token) {
      console.error("Missing id or token.");
      return null;
    }

    const response = await fetch(
      `${PATIENT_API}/${encodeURIComponent(id)}/${encodeURIComponent(token)}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      console.error("Failed to fetch appointments.");
      return null;
    }

    const result = await parseJsonSafe(response);

    if (Array.isArray(result)) {
      return result;
    }

    if (Array.isArray(result?.appointments)) {
      return result.appointments;
    }

    return [];
  } catch (error) {
    console.error("Error fetching appointments:", error);
    return null;
  }
}

/*
  Function: filterAppointments
  Purpose: Filter appointments by condition and patient name
*/
export async function filterAppointments(condition, name, token) {
  try {
    const safeCondition =
      condition && condition.trim() !== ""
        ? encodeURIComponent(condition.trim())
        : "null";

    const safeName =
      name && name.trim() !== ""
        ? encodeURIComponent(name.trim())
        : "null";

    const safeToken =
      token && token.trim() !== ""
        ? encodeURIComponent(token.trim())
        : "null";

    const response = await fetch(
      `${PATIENT_API}/appointments/filter/${safeCondition}/${safeName}/${safeToken}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      console.error("Failed to filter appointments.");
      return [];
    }

    const result = await parseJsonSafe(response);

    if (Array.isArray(result)) {
      return result;
    }

    if (Array.isArray(result?.appointments)) {
      return result.appointments;
    }

    return [];
  } catch (error) {
    console.error("Error filtering appointments:", error);
    alert("Something went wrong while filtering appointments.");
    return [];
  }
}