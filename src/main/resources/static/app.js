const externalFigures = ["Pentagono", "Hexagono"];

const fieldConfig = {
    Circulo: [{ name: "radio", label: "Radio" }],
    Cuadrado: [{ name: "lado", label: "Lado" }],
    Rectangulo: [
        { name: "base", label: "Base" },
        { name: "altura", label: "Altura" }
    ],
    TrianguloEquilatero: [{ name: "lado", label: "Lado" }],
    Pentagono: [{ name: "lado", label: "Lado" }],
    Hexagono: [{ name: "lado", label: "Lado" }]
};

const state = {
    nativas: [],
    dinamicas: [],
    proveedorDisponible: false,
    figuraSeleccionada: ""
};

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("refresh-button").addEventListener("click", refreshState);
    document.getElementById("figure-select").addEventListener("change", (event) => {
        state.figuraSeleccionada = event.target.value;
        renderParameters();
        clearResult();
    });
    document.getElementById("figure-form").addEventListener("submit", calculateFigure);
    renderProcess();
    renderExternalFigures();
    refreshState();
});

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    let body = null;
    const contentType = response.headers.get("content-type") || "";
    if (response.status !== 204 && contentType.includes("application/json")) {
        body = await response.json();
    }
    if (!response.ok) {
        throw new Error(body?.error || body?.mensaje || `La solicitud devolvió HTTP ${response.status}`);
    }
    return body;
}

async function refreshState() {
    try {
        const [availableResult, providerResult] = await Promise.allSettled([
            fetchJson("/api/figuras/disponibles"),
            fetchJson("/api/figuras/proveedor/estado")
        ]);

        if (availableResult.status === "fulfilled") {
            state.nativas = availableResult.value.nativas || [];
            state.dinamicas = availableResult.value.dinamicas || [];
            renderSystemFigures();
            renderSelector();
        }

        state.proveedorDisponible = providerResult.status === "fulfilled" && providerResult.value.conectado === true;
        renderProviderStatus();
        renderExternalFigures();

        if (!state.proveedorDisponible) {
            setError("El proveedor externo no está disponible. Inicia figuras-provider en el puerto 18081 para cargar figuras dinámicas.");
        } else if (availableResult.status === "rejected") {
            setError("No se pudo consultar el estado de las figuras en la aplicación principal.");
        } else {
            clearError();
        }
    } catch (error) {
        state.proveedorDisponible = false;
        renderProviderStatus();
        setError(error.message);
    }
}

function renderSystemFigures() {
    const nativeList = document.getElementById("native-list");
    nativeList.replaceChildren(...state.nativas.map((name) => createFigureItem(name, false)));

    const dynamicList = document.getElementById("dynamic-list");
    dynamicList.replaceChildren(...state.dinamicas.map((name) => createFigureItem(name, true)));
    document.getElementById("dynamic-empty").classList.toggle("hidden", state.dinamicas.length > 0);
}

function createFigureItem(name, dynamic) {
    const item = document.createElement("li");
    item.textContent = name;
    if (dynamic) {
        const check = document.createElement("span");
        check.className = "check";
        check.textContent = "✓";
        item.appendChild(check);
    }
    return item;
}

function renderProviderStatus() {
    const status = document.getElementById("provider-status");
    status.classList.toggle("connected", state.proveedorDisponible);
    status.classList.toggle("unavailable", !state.proveedorDisponible);
    document.getElementById("provider-status-label").textContent = state.proveedorDisponible ? "Conectado" : "No disponible";
    document.getElementById("provider-message").textContent = state.proveedorDisponible
        ? "Entrega definiciones Groovy mediante HTTP."
        : "No se puede contactar al proveedor en este momento.";
}

function renderExternalFigures() {
    const container = document.getElementById("external-figures");
    container.replaceChildren(...externalFigures.map((name) => {
        const loaded = state.dinamicas.includes(name);
        const card = document.createElement("article");
        card.className = "external-card";

        const description = document.createElement("div");
        const title = document.createElement("h3");
        title.textContent = name;
        const detail = document.createElement("p");
        detail.textContent = "Definición Groovy disponible en el proveedor";
        description.append(title, detail);

        const meta = document.createElement("div");
        meta.className = "external-meta";
        const loadState = document.createElement("span");
        loadState.className = `load-state${loaded ? " loaded" : ""}`;
        loadState.textContent = loaded ? "CARGADO ✓" : "NO CARGADO";
        const button = document.createElement("button");
        button.className = "button button-light load-external";
        button.type = "button";
        button.textContent = loaded ? "Cargada" : "Obtener y cargar";
        button.disabled = !state.proveedorDisponible || loaded;
        button.addEventListener("click", () => loadExternalFigure(name, button));
        meta.append(loadState, button);
        card.append(description, meta);
        return card;
    }));
}

function renderSelector() {
    const select = document.getElementById("figure-select");
    const available = [...new Set([...state.nativas, ...state.dinamicas])];
    const previous = state.figuraSeleccionada;
    select.replaceChildren(...available.map((name) => {
        const option = document.createElement("option");
        option.value = name;
        option.textContent = name;
        return option;
    }));
    if (available.length === 0) {
        state.figuraSeleccionada = "";
        renderParameters();
        return;
    }
    state.figuraSeleccionada = available.includes(previous) ? previous : available[0];
    select.value = state.figuraSeleccionada;
    renderParameters();
}

function renderParameters() {
    const container = document.getElementById("parameters-container");
    const fields = fieldConfig[state.figuraSeleccionada] || [];
    container.replaceChildren(...fields.map((field) => {
        const wrapper = document.createElement("div");
        wrapper.className = "parameter-field";
        const label = document.createElement("label");
        label.className = "field-label";
        label.htmlFor = `parameter-${field.name}`;
        label.textContent = field.label;
        const input = document.createElement("input");
        input.className = "input parameter-input";
        input.id = `parameter-${field.name}`;
        input.name = field.name;
        input.type = "number";
        input.min = "0";
        input.step = "any";
        input.placeholder = "Ej. 5";
        input.required = true;
        wrapper.append(label, input);
        return wrapper;
    }));
}

async function loadExternalFigure(name, clickedButton) {
    clearError();
    clearResult();
    resetProcess();
    clickedButton.disabled = true;
    document.getElementById("last-action").textContent = `Cargando ${name}...`;
    updateStep(1, "active", `Solicitando ${name} al servicio externo...`);

    try {
        await fetchJson(`/api/figuras/cargar-externa/${encodeURIComponent(name)}`, { method: "POST" });
        updateStep(1, "done");
        await pause(180);
        updateStep(2, "done");
        await pause(180);
        updateStep(3, "done");
        await pause(180);
        updateStep(4, "done");
        await pause(180);
        updateStep(5, "done", `${name} disponible ✓`);
        document.getElementById("last-action").textContent = `${name} cargada sin reiniciar Spring`;
        await refreshState();
        state.figuraSeleccionada = name;
        renderSelector();
        document.getElementById("figure-select").value = name;
        renderParameters();
    } catch (error) {
        updateStep(1, "error");
        document.getElementById("last-action").textContent = "La carga no se completó";
        setError(error.message || "No se pudo cargar la figura externa.");
        clickedButton.disabled = !state.proveedorDisponible;
    }
}

async function calculateFigure(event) {
    event.preventDefault();
    clearError();
    clearResult();
    if (!state.figuraSeleccionada) {
        setError("Selecciona una figura para calcular.");
        return;
    }

    const parametros = {};
    let invalid = false;
    document.querySelectorAll(".parameter-input").forEach((input) => {
        const value = Number(input.value);
        if (input.value.trim() === "" || !Number.isFinite(value) || value < 0) {
            invalid = true;
        } else {
            parametros[input.name] = value;
        }
    });
    if (invalid) {
        setError("Introduce valores numéricos válidos y mayores o iguales que cero.");
        return;
    }

    const button = document.getElementById("calculate-button");
    button.disabled = true;
    try {
        const result = await fetchJson("/api/figuras/crear", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ clase: state.figuraSeleccionada, parametros })
        });
        document.getElementById("result-area").textContent = formatNumber(result.area);
        document.getElementById("result-perimeter").textContent = formatNumber(result.perimetro);
        document.getElementById("result").classList.remove("hidden");
    } catch (error) {
        setError(error.message || "No se pudo calcular la figura.");
    } finally {
        button.disabled = false;
    }
}

function renderProcess() {
    resetProcess();
}

function resetProcess() {
    document.querySelectorAll("#process-list .step").forEach((step) => {
        step.className = "step pending";
    });
    document.getElementById("last-action").textContent = "Esperando una figura";
}

function updateStep(number, status, text) {
    const step = document.querySelector(`#process-list [data-step="${number}"]`);
    if (!step) return;
    step.className = `step ${status}`;
    if (text) step.lastElementChild.textContent = text;
}

function setError(message) {
    const error = document.getElementById("ui-error");
    error.textContent = message;
    error.classList.remove("hidden");
}

function clearError() {
    document.getElementById("ui-error").classList.add("hidden");
}

function clearResult() {
    document.getElementById("result").classList.add("hidden");
}

function formatNumber(value) {
    return Number(value.toFixed(6)).toString();
}

function pause(milliseconds) {
    return new Promise((resolve) => setTimeout(resolve, milliseconds));
}
