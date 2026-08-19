// Base path of the deployed app - adjust if your context path differs
const API_BASE = "api";

// ----- Login -----
document.getElementById("login-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  const res = await fetch(`${API_BASE}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
  });
  const data = await res.json();

  const msg = document.getElementById("login-message");
  msg.textContent = data.message;

  if (data.success) {
    document.getElementById("login-section").classList.add("hidden");
    document.getElementById("app-section").classList.remove("hidden");
    loadDentistOptions();
    loadTreatmentOptions();
  }
});

// ----- Logout -----
// Calls the server so the session is actually invalidated, not just
// hidden in the UI — otherwise the old session stays valid and someone
// could still hit the API endpoints directly until the tab is closed.
document.getElementById("logout-btn").addEventListener("click", async () => {
  await fetch(`${API_BASE}/logout`, { method: "POST" });

  document.getElementById("app-section").classList.add("hidden");
  document.getElementById("login-section").classList.remove("hidden");

  // Reset the login form/message and jump back to the first tab so the
  // next login starts from a clean, predictable state.
  document.getElementById("login-form").reset();
  document.getElementById("login-message").textContent = "";
  document.querySelectorAll(".tab").forEach((tab) => tab.classList.add("hidden"));
  document.getElementById("register").classList.remove("hidden");
});

// ----- Shared: fill a <select> from a simple {id, name} list endpoint -----
// idField/nameField let this work for both dentists (dentistId/name) and
// treatment types (treatmentId/name) without duplicating the loop.
async function loadLookupOptions(selectId, url, idField, nameField, placeholder, formatLabel) {
  const select = document.getElementById(selectId);

  try {
    const res = await fetch(url);
    const data = await res.json();

    select.innerHTML = "";
    const placeholderOpt = document.createElement("option");
    placeholderOpt.value = "";
    placeholderOpt.disabled = true;
    placeholderOpt.selected = true;
    placeholderOpt.textContent = placeholder;
    select.appendChild(placeholderOpt);

    if (!res.ok || !Array.isArray(data)) {
      return;
    }

    data.forEach((item) => {
      const option = document.createElement("option");
      option.value = item[idField];
      option.textContent = formatLabel ? formatLabel(item) : item[nameField];
      select.appendChild(option);
    });
  } catch (err) {
    select.innerHTML = '<option value="" disabled selected>Could not load options</option>';
  }
}

function loadDentistOptions() {
  loadLookupOptions(
      "register-dentist",
      `${API_BASE}/dentists`,
      "dentistId",
      "name",
      "Select a dentist",
      (d) => (d.specialty ? `${d.name} — ${d.specialty}` : d.name)
  );
}

function loadTreatmentOptions() {
  loadLookupOptions(
      "register-treatment",
      `${API_BASE}/treatments`,
      "name",   // value submitted with the form must be the name: appointment.treatment
      "name",   // is a free-text column (no FK to treatment_type), and billing looks
      "Select a treatment type",   // the price up by matching this name.
      (t) => `${t.name} — Rs. ${Number(t.price).toFixed(2)}`
  );
}

// ----- Tab navigation -----
// Scoped to buttons that actually have a data-tab attribute — the Logout
// button lives in the same <nav> but isn't a tab, so it's excluded here
// (its own click handler is set up separately, above).
document.querySelectorAll("nav button[data-tab]").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach((tab) => tab.classList.add("hidden"));
    document.getElementById(btn.dataset.tab).classList.remove("hidden");

    // Auto-load the appointment list whenever these tabs are opened,
    // so staff see it immediately without an extra click.
    if (btn.dataset.tab === "search") {
      loadAppointmentsTable("appointments-table", "appointments-table-body", false);
    }
    if (btn.dataset.tab === "cancel") {
      loadAppointmentsTable("cancel-table", "cancel-table-body", true);
    }
    if (btn.dataset.tab === "billing") {
      loadApptNumberOptions("billing-apptNumber");
    }
    if (btn.dataset.tab === "manage") {
      loadDentistsTable();
      loadTreatmentsTable();
    }
  });
});

// ----- Register appointment -----
document.getElementById("register-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = new URLSearchParams(new FormData(e.target));

  const res = await fetch(`${API_BASE}/appointments`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: form.toString()
  });
  const data = await res.json();

  const messageEl = document.getElementById("register-message");
  if (data.success) {
    messageEl.textContent = `Registered successfully — Appointment Number: ${data.apptNumber}`;
    e.target.reset();
    // reset() can leave the dentist/treatment <select>s on whatever option
    // ended up at index 0 after we replaced their contents dynamically, so
    // explicitly put them back on the "Select…" placeholder.
    loadDentistOptions();
    loadTreatmentOptions();
  } else {
    messageEl.textContent = data.message || "Registration failed";
  }
});

// ----- Search appointment (single, by number) -----
document.getElementById("search-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const apptNumber = document.getElementById("search-apptNumber").value.trim();
  const resultEl = document.getElementById("search-result");

  if (!apptNumber) {
    resultEl.textContent = "Enter an appointment number, or click \"Show All Appointments\".";
    return;
  }

  document.getElementById("appointments-table").classList.add("hidden");

  const res = await fetch(`${API_BASE}/appointments/search?apptNumber=${encodeURIComponent(apptNumber)}`);
  const data = await res.json();

  if (data.found === false) {
    resultEl.textContent = data.message;
  } else {
    resultEl.textContent =
        `Appointment #${data.apptNumber}\n` +
        `Patient: ${data.patient ? data.patient.name : "N/A"}\n` +
        `Dentist: ${data.dentist ? data.dentist.name : "N/A"}\n` +
        `Treatment: ${data.treatment}\n` +
        `Date: ${data.apptDate}   Time: ${data.apptTime}\n` +
        `Status: ${data.status}`;
  }
});

// ----- Shared: load all appointments into a table -----
// tableId/tbodyId: which table to fill. withCancelButton: if true, adds a
// "Cancel" button per row (used on the Cancel Appointment tab); rows whose
// status is already CANCELLED show plain text instead of a button.
async function loadAppointmentsTable(tableId, tbodyId, withCancelButton) {
  const table = document.getElementById(tableId);
  const tbody = document.getElementById(tbodyId);

  const res = await fetch(`${API_BASE}/appointments/all`);
  const data = await res.json();

  if (!res.ok || !Array.isArray(data)) {
    table.classList.add("hidden");
    return;
  }

  tbody.innerHTML = "";

  data.forEach((appt) => {
    const row = document.createElement("tr");
    const actionCell = withCancelButton
        ? `<td>${appt.status === "CANCELLED"
            ? "—"
            : `<button type="button" class="cancel-row-btn" data-appt-number="${appt.apptNumber}">Cancel</button>`}</td>`
        : "";

    row.innerHTML = `
      <td>${appt.apptNumber}</td>
      <td>${appt.patient ? appt.patient.name : "N/A"}</td>
      <td>${appt.dentist ? appt.dentist.name : "N/A"}</td>
      <td>${appt.treatment}</td>
      <td>${appt.apptDate}</td>
      <td>${appt.apptTime}</td>
      <td>${appt.status}</td>
      ${actionCell}
    `;
    tbody.appendChild(row);
  });

  table.classList.remove("hidden");
}

// ----- Shared: fill a <select> with appointment numbers -----
// Used by the Billing tab so staff pick an existing appointment instead of
// typing its number by hand. Cancelled appointments are excluded since a
// bill shouldn't be raised against them.
async function loadApptNumberOptions(selectId) {
  const select = document.getElementById(selectId);
  const previousValue = select.value;

  const res = await fetch(`${API_BASE}/appointments/all`);
  const data = await res.json();

  select.innerHTML = '<option value="" disabled selected>Select an appointment</option>';

  if (!res.ok || !Array.isArray(data)) {
    return;
  }

  data
      .filter((appt) => appt.status !== "CANCELLED")
      .forEach((appt) => {
        const option = document.createElement("option");
        option.value = appt.apptNumber;
        const patientName = appt.patient ? appt.patient.name : "N/A";
        option.textContent = `#${appt.apptNumber} — ${patientName} (${appt.apptDate})`;
        select.appendChild(option);
      });

  // Re-select whatever was chosen before, if it's still in the list.
  if (previousValue && select.querySelector(`option[value="${previousValue}"]`)) {
    select.value = previousValue;
  }
}

// ----- Cancel appointment (click a row's Cancel button) -----
document.getElementById("cancel-table-body").addEventListener("click", async (e) => {
  if (!e.target.classList.contains("cancel-row-btn")) {
    return;
  }
  const apptNumber = e.target.dataset.apptNumber;

  const res = await fetch(`${API_BASE}/appointments/cancel`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `apptNumber=${encodeURIComponent(apptNumber)}`
  });
  const data = await res.json();
  document.getElementById("cancel-message").textContent = data.message;

  // Refresh the table so the row shows its updated CANCELLED status.
  loadAppointmentsTable("cancel-table", "cancel-table-body", true);
});

// ----- Billing -----
document.getElementById("billing-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = new URLSearchParams(new FormData(e.target));

  const res = await fetch(`${API_BASE}/bill?${form.toString()}`);
  const data = await res.json();
  const receiptEl = document.getElementById("billing-receipt");
  const messageEl = document.getElementById("billing-message");
  const printBtn = document.getElementById("print-billing-btn");

  if (data.billId) {
    const appt = data.appointment || {};
    const patientName = appt.patient ? appt.patient.name : "N/A";
    const dentistName = appt.dentist ? appt.dentist.name : "N/A";
    const money = (n) => `Rs. ${Number(n).toFixed(2)}`;
    const tax = data.totalAmount - data.consultFee - data.treatmentCost - data.hospitalCharge;

    document.getElementById("r-billId").textContent = data.billId;
    document.getElementById("r-apptNumber").textContent = appt.apptNumber || form.get("apptNumber");
    document.getElementById("r-patient").textContent = patientName;
    document.getElementById("r-dentist").textContent = dentistName;
    document.getElementById("r-treatment").textContent = appt.treatment || "N/A";
    document.getElementById("r-datetime").textContent =
        `${appt.apptDate || ""} ${appt.apptTime || ""}`.trim() || "N/A";
    document.getElementById("r-consultFee").textContent = money(data.consultFee);
    document.getElementById("r-treatmentCost").textContent = money(data.treatmentCost);
    document.getElementById("r-hospitalCharge").textContent = money(data.hospitalCharge);
    document.getElementById("r-taxLabel").textContent = `Tax (${data.taxPercentage}%)`;
    document.getElementById("r-tax").textContent = money(tax);
    document.getElementById("r-total").textContent = money(data.totalAmount);

    receiptEl.classList.remove("hidden");
    messageEl.textContent = "";
    printBtn.classList.remove("hidden");
  } else {
    receiptEl.classList.add("hidden");
    messageEl.textContent = data.message || "Could not generate bill";
    printBtn.classList.add("hidden");
  }
});

document.getElementById("print-billing-btn").addEventListener("click", () => {
  window.print();
});

// ----- Manage: Add Dentist -----
document.getElementById("add-dentist-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = new URLSearchParams(new FormData(e.target));

  const res = await fetch(`${API_BASE}/dentists`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: form.toString()
  });
  const data = await res.json();

  const messageEl = document.getElementById("add-dentist-message");
  if (data.success) {
    messageEl.textContent = `Added — Dentist ID: ${data.dentistId}`;
    e.target.reset();
    loadDentistsTable();
    // Keep the Register Appointment dentist dropdown in sync too.
    loadDentistOptions();
  } else {
    messageEl.textContent = data.message || "Failed to add dentist";
  }
});

// ----- Manage: Add Treatment Type -----
document.getElementById("add-treatment-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = new URLSearchParams(new FormData(e.target));

  const res = await fetch(`${API_BASE}/treatments`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: form.toString()
  });
  const data = await res.json();

  const messageEl = document.getElementById("add-treatment-message");
  if (data.success) {
    messageEl.textContent = `Added — Treatment ID: ${data.treatmentId}`;
    e.target.reset();
    loadTreatmentsTable();
    // Keep the Register Appointment treatment dropdown in sync too.
    loadTreatmentOptions();
  } else {
    messageEl.textContent = data.message || "Failed to add treatment type";
  }
});

// ----- Manage: list tables -----
async function loadDentistsTable() {
  const tbody = document.getElementById("dentists-table-body");
  const res = await fetch(`${API_BASE}/dentists`);
  const data = await res.json();

  tbody.innerHTML = "";
  if (!res.ok || !Array.isArray(data)) {
    return;
  }

  data.forEach((d) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${d.dentistId}</td>
      <td>${d.name}</td>
      <td>${d.specialty || ""}</td>
    `;
    tbody.appendChild(row);
  });
}

async function loadTreatmentsTable() {
  const tbody = document.getElementById("treatments-table-body");
  const res = await fetch(`${API_BASE}/treatments`);
  const data = await res.json();

  tbody.innerHTML = "";
  if (!res.ok || !Array.isArray(data)) {
    return;
  }

  data.forEach((t) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${t.treatmentId}</td>
      <td>${t.name}</td>
      <td>Rs. ${Number(t.price).toFixed(2)}</td>
    `;
    tbody.appendChild(row);
  });
}