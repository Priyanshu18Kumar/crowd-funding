// ============================================================
//  app.js  -  shared JavaScript for the Crowdfunding frontend
// ------------------------------------------------------------
//  All the data (users, campaigns, contributions) is stored in
//  the browser using localStorage, so the pages work by simply
//  opening them - no server needed. This keeps things simple
//  for a beginner project.
// ============================================================


// ---------- generic storage helpers ----------

// Read a value from localStorage and turn it back into a JS object/array
function read(key, fallback) {
  var raw = localStorage.getItem(key);
  if (raw === null) return fallback;
  try {
    return JSON.parse(raw);
  } catch (e) {
    return fallback;
  }
}

// Save a value into localStorage as text
function write(key, value) {
  localStorage.setItem(key, JSON.stringify(value));
}


// ---------- collections ----------

function getUsers() { return read("users", []); }
function saveUsers(list) { write("users", list); }

function getCampaigns() { return read("campaigns", []); }
function saveCampaigns(list) { write("campaigns", list); }

function getContributions() { return read("contributions", []); }
function saveContributions(list) { write("contributions", list); }

function getSettings() {
  return read("settings", {
    "Platform Name": "CrowdFund",
    "Currency": "INR",
    "Allow New Campaigns": "yes"
  });
}
function saveSettings(s) { write("settings", s); }

function getLog() { return read("log", []); }
function addLog(message) {
  var list = getLog();
  list.push(message);
  write("log", list);
}


// ---------- id helper: give every new item a unique id ----------
function nextId(list) {
  var max = 0;
  for (var i = 0; i < list.length; i++) {
    if (list[i].id > max) max = list[i].id;
  }
  return max + 1;
}


// ---------- create a default admin the very first time ----------
function seed() {
  if (getUsers().length === 0) {
    saveUsers([
      { id: 1, name: "Platform Admin", email: "admin@crowd.com", password: "admin123", role: "Admin" }
    ]);
  }
}


// ---------- login / session ----------

function login(email, password) {
  var users = getUsers();
  for (var i = 0; i < users.length; i++) {
    if (users[i].email.toLowerCase() === email.toLowerCase() && users[i].password === password) {
      write("currentUser", users[i]);
      addLog("User logged in: " + users[i].email);
      return users[i];
    }
  }
  return null;
}

function currentUser() { return read("currentUser", null); }

function logout() {
  localStorage.removeItem("currentUser");
  window.location.href = "index.html";
}

function goToDashboard(role) {
  if (role === "Admin") {
    window.location.href = "admin.html";
  } else if (role === "Creator") {
    window.location.href = "creator.html";
  } else {
    window.location.href = "contributor.html";
  }
}

// Used at the top of every dashboard page. If the visitor is not
// logged in (or has the wrong role) they are sent back to the login page.
function requireRole(role) {
  var u = currentUser();
  if (u === null || u.role !== role) {
    window.location.href = "index.html";
    return null;
  }
  return u;
}


// ---------- small display helpers ----------

function money(amount) {
  return getSettings()["Currency"] + " " + amount;
}

function progressPercent(c) {
  if (c.goalAmount === 0) return 0;
  var p = (c.raisedAmount / c.goalAmount) * 100;
  if (p > 100) p = 100;
  return p;
}

function statusBadge(status) {
  return "<span class='badge " + status.toLowerCase() + "'>" + status + "</span>";
}

function today() {
  var d = new Date();
  var m = String(d.getMonth() + 1).padStart(2, "0");
  var day = String(d.getDate()).padStart(2, "0");
  return d.getFullYear() + "-" + m + "-" + day;
}

// Show a short message under a form (green for ok, red for error)
function showMessage(elementId, text, type) {
  var el = document.getElementById(elementId);
  if (el === null) return;
  el.textContent = text;
  el.className = "message " + type;
}


// ============================================================
//  PAGE: login / register  (index.html)
// ============================================================
function initIndex() {
  seed();

  // Already logged in? Go straight to the right dashboard.
  var already = currentUser();
  if (already !== null) {
    goToDashboard(already.role);
    return;
  }

  // Login form
  document.getElementById("loginForm").addEventListener("submit", function (e) {
    e.preventDefault();
    var email = document.getElementById("loginEmail").value.trim();
    var password = document.getElementById("loginPassword").value;
    var user = login(email, password);
    if (user === null) {
      showMessage("loginMessage", "Invalid email or password.", "error");
      return;
    }
    goToDashboard(user.role);
  });

  // Register form
  document.getElementById("registerForm").addEventListener("submit", function (e) {
    e.preventDefault();
    var name = document.getElementById("regName").value.trim();
    var email = document.getElementById("regEmail").value.trim();
    var password = document.getElementById("regPassword").value;
    var role = document.getElementById("regRole").value;

    var users = getUsers();
    for (var i = 0; i < users.length; i++) {
      if (users[i].email.toLowerCase() === email.toLowerCase()) {
        showMessage("registerMessage", "That email is already registered.", "error");
        return;
      }
    }

    users.push({ id: nextId(users), name: name, email: email, password: password, role: role });
    saveUsers(users);
    addLog("New " + role + " registered: " + email);
    showMessage("registerMessage", "Registration successful! You can now log in.", "ok");
    document.getElementById("registerForm").reset();
  });
}


// ============================================================
//  PAGE: admin dashboard  (admin.html)
// ============================================================
function initAdmin() {
  var admin = requireRole("Admin");
  if (admin === null) return;

  document.getElementById("adminName").textContent = admin.name;
  document.getElementById("logoutBtn").addEventListener("click", logout);

  // Buttons that switch between the four panels
  var buttons = document.querySelectorAll(".nav-btn");
  for (var i = 0; i < buttons.length; i++) {
    buttons[i].addEventListener("click", function () {
      showPanel(this.getAttribute("data-panel"));
    });
  }

  // Add-user form
  document.getElementById("addUserForm").addEventListener("submit", function (e) {
    e.preventDefault();
    var users = getUsers();
    var email = document.getElementById("newUserEmail").value.trim();

    for (var i = 0; i < users.length; i++) {
      if (users[i].email.toLowerCase() === email.toLowerCase()) {
        showMessage("addUserMessage", "Email already in use.", "error");
        return;
      }
    }

    users.push({
      id: nextId(users),
      name: document.getElementById("newUserName").value.trim(),
      email: email,
      password: document.getElementById("newUserPassword").value,
      role: document.getElementById("newUserRole").value
    });
    saveUsers(users);
    addLog("Admin added user: " + email);
    showMessage("addUserMessage", "User created successfully.", "ok");
    document.getElementById("addUserForm").reset();
    renderUsers();
  });

  // Save settings button
  document.getElementById("saveSettingsBtn").addEventListener("click", saveAllSettings);

  renderUsers();
  renderCampaigns();
  renderSettings();
  renderActivity();
}

function showPanel(id) {
  var panels = document.querySelectorAll(".panel");
  for (var i = 0; i < panels.length; i++) {
    panels[i].style.display = "none";
  }
  document.getElementById(id).style.display = "block";

  var buttons = document.querySelectorAll(".nav-btn");
  for (var i = 0; i < buttons.length; i++) {
    buttons[i].classList.remove("active");
    if (buttons[i].getAttribute("data-panel") === id) {
      buttons[i].classList.add("active");
    }
  }
}

function renderUsers() {
  var users = getUsers();
  var body = document.getElementById("usersBody");
  body.innerHTML = "";
  for (var i = 0; i < users.length; i++) {
    var u = users[i];
    var row = document.createElement("tr");
    row.innerHTML =
      "<td>" + u.id + "</td>" +
      "<td>" + u.name + "</td>" +
      "<td>" + u.email + "</td>" +
      "<td>" + u.role + "</td>" +
      "<td><button class='small danger' onclick='deleteUser(" + u.id + ")'>Delete</button></td>";
    body.appendChild(row);
  }
}

function deleteUser(id) {
  var users = getUsers();
  for (var i = 0; i < users.length; i++) {
    if (users[i].id === id) {
      users.splice(i, 1);
      break;
    }
  }
  saveUsers(users);
  addLog("Admin deleted a user (id " + id + ")");
  renderUsers();
}

function renderCampaigns() {
  var campaigns = getCampaigns();
  var body = document.getElementById("campaignsBody");
  body.innerHTML = "";
  for (var i = 0; i < campaigns.length; i++) {
    var c = campaigns[i];
    var actions;
    if (c.status === "PENDING") {
      actions =
        "<button class='small' onclick='approveCampaign(" + c.id + ")'>Approve</button> " +
        "<button class='small danger' onclick='rejectCampaign(" + c.id + ")'>Reject</button>";
    } else {
      actions = "<span class='muted'>No action</span>";
    }
    var row = document.createElement("tr");
    row.innerHTML =
      "<td>" + c.id + "</td>" +
      "<td>" + c.title + "</td>" +
      "<td>" + money(c.goalAmount) + "</td>" +
      "<td>" + money(c.raisedAmount) + "</td>" +
      "<td>" + statusBadge(c.status) + "</td>" +
      "<td>" + actions + "</td>";
    body.appendChild(row);
  }
}

function approveCampaign(id) { setCampaignStatus(id, "APPROVED"); }
function rejectCampaign(id) { setCampaignStatus(id, "REJECTED"); }

function setCampaignStatus(id, status) {
  var campaigns = getCampaigns();
  for (var i = 0; i < campaigns.length; i++) {
    if (campaigns[i].id === id) {
      campaigns[i].status = status;
      break;
    }
  }
  saveCampaigns(campaigns);
  addLog("Campaign " + status.toLowerCase() + " (id " + id + ")");
  renderCampaigns();
}

function renderSettings() {
  var settings = getSettings();
  var box = document.getElementById("settingsBox");
  box.innerHTML = "";
  var keys = Object.keys(settings);
  for (var i = 0; i < keys.length; i++) {
    var key = keys[i];
    var row = document.createElement("div");
    row.className = "setting-row";
    row.innerHTML =
      "<label>" + key + "</label>" +
      "<input type='text' value='" + settings[key] + "' data-key='" + key + "'>";
    box.appendChild(row);
  }
}

function saveAllSettings() {
  var inputs = document.querySelectorAll("#settingsBox input");
  var settings = getSettings();
  for (var i = 0; i < inputs.length; i++) {
    settings[inputs[i].getAttribute("data-key")] = inputs[i].value;
  }
  saveSettings(settings);
  addLog("System settings updated");
  showMessage("settingsMessage", "Settings updated successfully.", "ok");
}

function renderActivity() {
  var log = getLog();
  var box = document.getElementById("activityBox");
  box.innerHTML = "";
  if (log.length === 0) {
    box.innerHTML = "<p class='muted'>No activity yet.</p>";
    return;
  }
  var ol = document.createElement("ol");
  for (var i = 0; i < log.length; i++) {
    var li = document.createElement("li");
    li.textContent = log[i];
    ol.appendChild(li);
  }
  box.appendChild(ol);
}


// ============================================================
//  PAGE: creator dashboard  (creator.html)
// ============================================================
function initCreator() {
  var creator = requireRole("Creator");
  if (creator === null) return;

  document.getElementById("creatorName").textContent = creator.name;
  document.getElementById("logoutBtn").addEventListener("click", logout);

  // Create campaign
  document.getElementById("createCampaignForm").addEventListener("submit", function (e) {
    e.preventDefault();
    var campaigns = getCampaigns();
    var campaign = {
      id: nextId(campaigns),
      title: document.getElementById("campTitle").value.trim(),
      description: document.getElementById("campDesc").value.trim(),
      goalAmount: Number(document.getElementById("campGoal").value),
      raisedAmount: 0,
      creatorEmail: creator.email,
      status: "PENDING",
      updates: []
    };
    campaigns.push(campaign);
    saveCampaigns(campaigns);
    addLog("Campaign created: " + campaign.title + " by " + creator.email);
    showMessage("createMessage", "Campaign created (ID " + campaign.id + "). Waiting for admin approval.", "ok");
    document.getElementById("createCampaignForm").reset();
    renderCreatorCampaigns(creator);
    renderCreatorAnalytics(creator);
  });

  // Post a progress update
  document.getElementById("updateForm").addEventListener("submit", function (e) {
    e.preventDefault();
    var id = Number(document.getElementById("updateCampaignId").value);
    var msg = document.getElementById("updateMessage").value.trim();
    var campaigns = getCampaigns();
    for (var i = 0; i < campaigns.length; i++) {
      if (campaigns[i].id === id && campaigns[i].creatorEmail === creator.email) {
        campaigns[i].updates.push(msg);
        saveCampaigns(campaigns);
        addLog("Progress update posted on campaign id " + id);
        showMessage("updateMsg", "Progress update posted.", "ok");
        document.getElementById("updateForm").reset();
        renderCreatorCampaigns(creator);
        return;
      }
    }
    showMessage("updateMsg", "Campaign not found or not yours.", "error");
  });

  // Message contributors
  document.getElementById("messageForm").addEventListener("submit", function (e) {
    e.preventDefault();
    var id = Number(document.getElementById("msgCampaignId").value);
    var campaigns = getCampaigns();
    var found = null;
    for (var i = 0; i < campaigns.length; i++) {
      if (campaigns[i].id === id && campaigns[i].creatorEmail === creator.email) {
        found = campaigns[i];
      }
    }
    if (found === null) {
      showMessage("msgResult", "Campaign not found or not yours.", "error");
      return;
    }
    var count = 0;
    var cons = getContributions();
    for (var j = 0; j < cons.length; j++) {
      if (cons[j].campaignId === id) count++;
    }
    addLog("Message sent to contributors of campaign id " + id);
    showMessage("msgResult", "Message delivered to " + count + " contributor(s).", "ok");
    document.getElementById("messageForm").reset();
  });

  renderCreatorCampaigns(creator);
  renderCreatorAnalytics(creator);
}

function renderCreatorCampaigns(creator) {
  var campaigns = getCampaigns();
  var body = document.getElementById("myCampaignsBody");
  body.innerHTML = "";
  for (var i = 0; i < campaigns.length; i++) {
    var c = campaigns[i];
    if (c.creatorEmail !== creator.email) continue;
    var row = document.createElement("tr");
    row.innerHTML =
      "<td>" + c.id + "</td>" +
      "<td>" + c.title + "</td>" +
      "<td>" + money(c.goalAmount) + "</td>" +
      "<td>" + money(c.raisedAmount) + "</td>" +
      "<td>" + progressPercent(c).toFixed(1) + "%</td>" +
      "<td>" + statusBadge(c.status) + "</td>";
    body.appendChild(row);
  }
}

function renderCreatorAnalytics(creator) {
  var campaigns = getCampaigns();
  var box = document.getElementById("analyticsBox");
  box.innerHTML = "";
  var found = false;
  for (var i = 0; i < campaigns.length; i++) {
    var c = campaigns[i];
    if (c.creatorEmail !== creator.email) continue;
    found = true;

    var count = 0;
    var cons = getContributions();
    for (var j = 0; j < cons.length; j++) {
      if (cons[j].campaignId === c.id) count++;
    }

    var card = document.createElement("div");
    card.className = "card";
    card.innerHTML =
      "<h4>" + c.title + " " + statusBadge(c.status) + "</h4>" +
      "<p>Contributors: " + count + "</p>" +
      "<p>Total raised: " + money(c.raisedAmount) + " / " + money(c.goalAmount) + "</p>" +
      "<div class='progress'><div class='bar' style='width:" + progressPercent(c).toFixed(1) + "%'></div></div>";
    box.appendChild(card);
  }
  if (!found) box.innerHTML = "<p class='muted'>No campaigns yet.</p>";
}


// ============================================================
//  PAGE: contributor dashboard  (contributor.html)
// ============================================================
function initContributor() {
  var contributor = requireRole("Contributor");
  if (contributor === null) return;

  document.getElementById("contributorName").textContent = contributor.name;
  document.getElementById("logoutBtn").addEventListener("click", logout);

  // Fill the profile form with the current details
  document.getElementById("profileName").value = contributor.name;
  document.getElementById("profileEmail").value = contributor.email;

  // Update profile
  document.getElementById("profileForm").addEventListener("submit", function (e) {
    e.preventDefault();
    var newName = document.getElementById("profileName").value.trim();
    var newEmail = document.getElementById("profileEmail").value.trim();
    var users = getUsers();

    for (var i = 0; i < users.length; i++) {
      if (users[i].email.toLowerCase() === newEmail.toLowerCase() && users[i].id !== contributor.id) {
        showMessage("profileMessage", "That email is already in use.", "error");
        return;
      }
    }

    for (var i = 0; i < users.length; i++) {
      if (users[i].id === contributor.id) {
        users[i].name = newName;
        users[i].email = newEmail;
        write("currentUser", users[i]);
      }
    }
    saveUsers(users);
    showMessage("profileMessage", "Profile updated successfully.", "ok");
    document.getElementById("contributorName").textContent = newName;
  });

  renderContributorCampaigns(contributor);
  renderMyContributions(contributor);
  renderUpdates();
}

function renderContributorCampaigns(contributor) {
  var campaigns = getCampaigns();
  var box = document.getElementById("campaignsBox");
  box.innerHTML = "";
  var found = false;
  for (var i = 0; i < campaigns.length; i++) {
    var c = campaigns[i];
    if (c.status !== "APPROVED") continue;
    found = true;
    var card = document.createElement("div");
    card.className = "card";
    card.innerHTML =
      "<h4>" + c.title + "</h4>" +
      "<p>" + c.description + "</p>" +
      "<p>Goal: " + money(c.goalAmount) + " | Raised: " + money(c.raisedAmount) + "</p>" +
      "<div class='progress'><div class='bar' style='width:" + progressPercent(c).toFixed(1) + "%'></div></div>" +
      "<div class='contribute-row'>" +
        "<input type='number' id='amount" + c.id + "' placeholder='Amount' min='1'>" +
        "<button class='small' onclick='contribute(" + c.id + ")'>Contribute</button>" +
      "</div>";
    box.appendChild(card);
  }
  if (!found) box.innerHTML = "<p class='muted'>No campaigns are open for contribution right now.</p>";
}

function contribute(campaignId) {
  var contributor = currentUser();
  var amount = Number(document.getElementById("amount" + campaignId).value);
  if (!(amount > 0)) {
    alert("Please enter a valid amount.");
    return;
  }

  var campaigns = getCampaigns();
  var campaign = null;
  for (var i = 0; i < campaigns.length; i++) {
    if (campaigns[i].id === campaignId) {
      campaign = campaigns[i];
    }
  }
  if (campaign === null || campaign.status !== "APPROVED") {
    alert("Campaign not available.");
    return;
  }

  campaign.raisedAmount += amount;
  saveCampaigns(campaigns);

  var cons = getContributions();
  cons.push({
    id: nextId(cons),
    campaignId: campaignId,
    campaignTitle: campaign.title,
    contributorEmail: contributor.email,
    amount: amount,
    date: today()
  });
  saveContributions(cons);
  addLog("Contribution of " + amount + " to " + campaign.title);

  alert("Contribution confirmed. Thank you!");
  renderContributorCampaigns(contributor);
  renderMyContributions(contributor);
}

function renderMyContributions(contributor) {
  var cons = getContributions();
  var body = document.getElementById("myContribBody");
  body.innerHTML = "";
  var total = 0;
  for (var i = 0; i < cons.length; i++) {
    if (cons[i].contributorEmail !== contributor.email) continue;
    total += cons[i].amount;
    var row = document.createElement("tr");
    row.innerHTML =
      "<td>" + cons[i].campaignTitle + "</td>" +
      "<td>" + money(cons[i].amount) + "</td>" +
      "<td>" + cons[i].date + "</td>";
    body.appendChild(row);
  }
  document.getElementById("totalContrib").textContent = money(total);
}

function renderUpdates() {
  var campaigns = getCampaigns();
  var box = document.getElementById("updatesBox");
  box.innerHTML = "";
  var found = false;
  for (var i = 0; i < campaigns.length; i++) {
    var c = campaigns[i];
    if (c.updates.length === 0) continue;
    found = true;
    var card = document.createElement("div");
    card.className = "card";
    var html = "<h4>" + c.title + "</h4><ul>";
    for (var j = 0; j < c.updates.length; j++) {
      html += "<li>" + c.updates[j] + "</li>";
    }
    html += "</ul>";
    card.innerHTML = html;
    box.appendChild(card);
  }
  if (!found) box.innerHTML = "<p class='muted'>No campaign updates yet.</p>";
}


// ============================================================
//  Start the right page based on <body data-page="...">
// ============================================================
document.addEventListener("DOMContentLoaded", function () {
  var page = document.body.getAttribute("data-page");
  if (page === "index") {
    initIndex();
  } else if (page === "admin") {
    initAdmin();
  } else if (page === "creator") {
    initCreator();
  } else if (page === "contributor") {
    initContributor();
  }
});
