// patientRows.js
export function createPatientRow(patient, appointmentId, doctorId) {
  const tr = document.createElement("tr");
  
  // Ensure patient object and its properties have safe values
  const patientId = patient?.id || "N/A";
  const patientName = patient?.name || "Unknown";
  const patientPhone = patient?.phone || "N/A";
  const patientEmail = patient?.email || "N/A";
  
  tr.innerHTML = `
      <td class="patient-id">${patientId}</td>
      <td>${patientName}</td>
      <td>${patientPhone}</td>
      <td>${patientEmail}</td>
      <td><img src="../assets/images/addPrescriptionIcon/addPrescription.png" alt="addPrescriptionIcon" class="prescription-btn" data-appointment-id="${appointmentId}" data-patient-name="${patientName}"></img></td>
    `;

  // Attach event listeners

  tr.querySelector(".prescription-btn").addEventListener("click", () => {
    window.location.href = `/pages/addPrescription.html?appointmentId=${appointmentId}&patientName=${patientName}`;
  });

  return tr;
}
