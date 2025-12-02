document.getElementById("action").addEventListener("change", function () {
  resetInputFields();
  resetGraph();

  const action = this.value;
  const inputFields = document.querySelectorAll(".input-field");
  inputFields.forEach((field) => (field.style.display = "none"));

  const sendButton = document.getElementById("send-btn");

  if (action === "get-network-root-reference-id-and-rel-date") {
    document.getElementById(
      "network-root-reference-id-container"
    ).style.display = "block";
    document.getElementById("network-rel-date-container").style.display =
      "block";
    sendButton.disabled = false;
  } else {
    sendButton.disabled = true;
  }
});

document
  .getElementById("send-btn")
  .addEventListener("click", async function () {
    document.getElementById("json-output").textContent = "";
    resetGraph();

    if (!validateForm()) {
      return;
    }

    const action = document.getElementById("action").value;
    let data;

    if (action === "get-network-root-reference-id-and-rel-date") {
      const rootReferenceId = document.getElementById(
        "network-root-reference-id"
      ).value;
      const relationshipDate =
        document.getElementById("network-rel-date").value;
      data = await getNetwork(rootReferenceId, relationshipDate);
    }

    if (!data) {
      document.getElementById("json-output").textContent =
        "An error occurred when getting network";
      return;
    } else if (data.message) {
      document.getElementById("json-output").textContent = data.message;
      return;
    }

    if (data.networks) {
      const jsonOutput = document.getElementById("json-output");
      jsonOutput.textContent = JSON.stringify(data.networks, null, 2);
    }

    initializeGraph(data);
  });

document.getElementById("copy-json-btn").addEventListener("click", function () {
  const text = document.getElementById("json-output").textContent;
  navigator.clipboard.writeText(text).then(() => {
    const tooltip = document.getElementById("copy-tooltip");
    tooltip.classList.add("show");
    setTimeout(() => {
      tooltip.classList.remove("show");
    }, 1500);
  });
});

function resetInputFields() {
  document.getElementById("network-root-reference-id").value = "";
  document.getElementById("network-rel-date").value = "";
  document.getElementById("network-root-reference-id-error").style.display =
    "none";
  document.getElementById("network-rel-date-error").style.display = "none";
  document.getElementById("json-output").textContent = "";
}

function resetGraph() {
  const cy = cytoscape({
    container: document.getElementById("cy"),
    elements: [],
    style: [],
    layout: {
      name: "cose",
      animate: true,
    },
  });
  cy.fit();
}

function validateForm() {
  let isValid = true;
  const networkRootReferenceContainer = document.getElementById(
    "network-root-reference-id-container"
  );

  if (networkRootReferenceContainer.style.display !== "none") {
    const inputValue = document
      .getElementById("network-root-reference-id")
      .value.trim();
    if (!inputValue) {
      document.getElementById("network-root-reference-id-error").style.display =
        "block";
      isValid = false;
    } else {
      document.getElementById("network-root-reference-id-error").style.display =
        "none";
    }
  }

  const relDateContainer = document.getElementById(
    "network-rel-date-container"
  );
  const inputValue = document.getElementById("network-rel-date").value.trim();
  if (relDateContainer.style.display !== "none" && inputValue !== "") {
    const regex = /^\d{4}-\d{2}-\d{2}$/;
    let isValidDate = false;

    if (regex.test(inputValue)) {
      const [year, month, day] = inputValue.split("-").map(Number);
      const date = new Date(year, month - 1, day); // month is 0-based
      isValidDate =
        date.getFullYear() == year &&
        date.getMonth() == month - 1 &&
        date.getDate() == day;
    }

    if (!isValidDate) {
      document.getElementById("network-rel-date-error").style.display = "block";
      isValid = false;
    } else {
      document.getElementById("network-rel-date-error").style.display = "none";
    }
  } else {
    document.getElementById("network-rel-date-error").style.display = "none";
  }

  return isValid;
}

function layoutConfig(edgesLength) {
  // Determine layout based on the presence of edges
  return edgesLength > 0
    ? {
        name: "cose",
        padding: 20,
        animate: true,
        fit: true,
        boundingBox: { x1: 0, y1: 0, x2: 900, y2: 400 },
      }
    : { name: "random", fit: true };
}

async function getNetwork(rootReferenceId, relationshipDate) {
  try {
    const url = `/spring-network-service/networks/graph/${encodeURIComponent(
      rootReferenceId
    )}?relationshipDate=${encodeURIComponent(relationshipDate)}`;
    const response = await fetch(url, { method: "GET" });
    const data = await response.json();
    console.log("getNetwork data:", data);

    if (!data || data.message) return data;

    return transformGraphData(data);
  } catch (error) {
    console.error("Error hitting getNetwork", error);
    return null;
  }
}

function transformGraphData(data) {
  const output = { elements: { nodes: [], edges: [] } };

  const vertices = Object.keys(data.vertices).map((vertexId) => ({
    data: {
      id: vertexId,
      label: data.vertices[vertexId].label,
      name: data.vertices[vertexId].name.replaceAll(" ", "\n"),
      properties: data.vertices[vertexId],
    },
  }));

  const edges = Object.keys(data.edges).map((edgeId) => ({
    data: {
      id: edgeId,
      source: data.edges[edgeId].from,
      target: data.edges[edgeId].to,
      label: data.edges[edgeId].label,
      properties: data.edges[edgeId],
    },
  }));

  output.elements.nodes.push(...vertices);
  output.elements.edges.push(...edges);
  output.networks = data.networks;

  return output;
}

function initializeGraph(data) {
  const cy = cytoscape({
    container: document.getElementById("cy"),
    elements: data.elements,
    style: [
      {
        selector: "node",
        style: {
          label: "data(name)",
          shape: "ellipse",
          color: "#fff",
          "background-color": "#0074d9",
          "text-valign": "center",
          "text-wrap": "wrap",
          "max-text-width": 60,
          "font-size": 14,
          "line-height": "1.2px",
          padding: "8px",
          width: 80,
          height: 80,
        },
      },
      {
        selector: 'node[label = "Selling Vertex"]',
        style: { "background-color": "#0074d9" },
      },
      {
        selector: 'node[label = "Non-Selling Vertex"]',
        style: { "background-color": "darkorange" },
      },
      {
        selector: "edge",
        style: {
          color: "#000000",
          width: 3,
          "curve-style": "bezier",
          "control-point-step-size": 80,
          "control-point-distance": 40,
          "arrow-scale": 1.2,
          "edge-distances": "node-position",
          "source-endpoint": "outside-to-node",
          "target-endpoint": "outside-to-node",
          "control-point-step-size": 20,
          "target-arrow-shape": "triangle",
          "target-arrow-color": "gray",
          "line-color": "gray",
          "text-background-color": "#fff",
          "text-background-opacity": 1,
          "text-background-padding": "3px",
        },
      },
      {
        selector: 'edge[label = "Selling Relationship"]',
        style: {
          "target-arrow-color": "gray",
          "line-color": "gray",
        },
      },
      {
        selector: 'edge[label = "Non-Selling Relationship"]',
        style: {
          "target-arrow-color": "darkorange",
          "line-color": "darkorange",
        },
      },
    ],
    layout: layoutConfig(data.elements.edges.length),
  });

  addOpenPopupFeature(cy);

  cy.on("layoutstop", () => {
    cy.resize();
    cy.fit();
  });
}

function addOpenPopupFeature(cy) {
  const popup = document.getElementById("popup");
  const popupContent = document.querySelector(".popup-content");
  const graphContainer = document.getElementById("graph-container");

  cy.on("tap", "node, edge", function (event) {
    const element = event.target;
    const properties = element.data("properties");

    // Create HTML content for popup
    let content = `<div class="property-label">${element.data("label")}</div>`;
    for (const key in properties) {
      if (properties.hasOwnProperty(key)) {
        content += `<div class="property-item"><strong>${key}:</strong> ${properties[key]}</div>`;
      }
    }

    const cyContainer = cy.container();
    const rect = cyContainer.getBoundingClientRect();
    const y = rect.top + event.renderedPosition.y;
    const x = rect.left + event.renderedPosition.x;

    const popupWidth = 200;
    const popupHeight = 150;
    const margin = 10;

    let posX = x + margin;
    let posY = y + margin;

    if (posX + popupWidth > window.innerWidth) {
      posX = x - popupWidth - margin;
    }

    if (posY + popupHeight > window.innerHeight) {
      posY = y - popupHeight - margin;
    }

    popupContent.innerHTML = content;
    popup.style.display = "block";
    popup.style.left = `${posX}px`;
    popup.style.top = `${posY}px`;
  });

  cy.on("tap", function (event) {
    if (event.target === cy) closePopup();
  });

  document.addEventListener("click", function (event) {
    if (
      !popup.contains(event.target) &&
      !graphContainer.contains(event.target)
    ) {
      closePopup();
    }
  });
}

function closePopup() {
  document.getElementById("popup").style.display = "none";
}
