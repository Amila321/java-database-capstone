// doctorServices.js

import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = API_BASE_URL + "/doctor";

async function parseJsonSafe(response) {
  try {
    return await response.json();
  } catch (error) {
    return null;
  }
}

export async function getDoctors() {
  try {
    const response = await fetch(DOCTOR_API, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    const data = await parseJsonSafe(response);

    if (!response.ok) {
      console.error("Failed to fetch doctors:", data);
      return [];
    }

    if (Array.isArray(data)) {
      return data;
    }

    if (Array.isArray(data?.doctors)) {
      return data.doctors;
    }

    return [];
  } catch (error) {
    console.error("Error fetching doctors:", error);
    return [];
  }
}

export async function deleteDoctor(id, token) {
  try {
    if (!id || !token) {
      return {
        success: false,
        message: "Doctor ID or token is missing.",
      };
    }

    const response = await fetch(
      `${DOCTOR_API}/${encodeURIComponent(id)}/${encodeURIComponent(token)}`,
      {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    const data = await parseJsonSafe(response);

    return {
      success: response.ok,
      message: data?.message || (response.ok ? "Doctor deleted successfully." : "Failed to delete doctor."),
    };
  } catch (error) {
    console.error("Error deleting doctor:", error);
    return {
      success: false,
      message: "An error occurred while deleting the doctor.",
    };
  }
}

export async function saveDoctor(doctor, token) {
  try {
    if (!doctor || !token) {
      return {
        success: false,
        message: "Doctor data or token is missing.",
      };
    }

    const response = await fetch(
      `${DOCTOR_API}/${encodeURIComponent(token)}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(doctor),
      }
    );

    const data = await parseJsonSafe(response);

    return {
      success: response.ok,
      message: data?.message || (response.ok ? "Doctor saved successfully." : "Failed to save doctor."),
    };
  } catch (error) {
    console.error("Error saving doctor:", error);
    return {
      success: false,
      message: "An error occurred while saving the doctor.",
    };
  }
}

export async function filterDoctors(name, time, specialty) {
  try {
    const safeName =
      name && name.trim() !== "" ? encodeURIComponent(name.trim()) : "null";
    const safeTime =
      time && time.trim() !== "" ? encodeURIComponent(time.trim()) : "null";
    const safeSpecialty =
      specialty && specialty.trim() !== ""
        ? encodeURIComponent(specialty.trim())
        : "null";

    const response = await fetch(
      `${DOCTOR_API}/filter/${safeName}/${safeTime}/${safeSpecialty}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    const data = await parseJsonSafe(response);

    if (!response.ok) {
      console.error("Failed to filter doctors:", data);
      return [];
    }

    if (Array.isArray(data)) {
      return data;
    }

    if (Array.isArray(data?.doctors)) {
      return data.doctors;
    }

    return [];
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("Something went wrong while filtering doctors.");
    return [];
  }
}