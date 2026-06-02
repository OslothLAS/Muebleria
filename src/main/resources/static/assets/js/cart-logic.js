/**
 * cart-logic.js
 * VERSIÓN DEFINITIVA Y LIMPIA
 */

// 1. ABRIR/CERRAR PANEL
function toggleCart() {
    const sideCart = document.querySelector('.side-cart');
    const overlay = document.querySelector('.cart-overlay');
    if (sideCart) sideCart.classList.toggle('active');
    if (overlay) overlay.classList.toggle('active');
}

async function updateSideQty(productId, change, btnElement) {
    const input = document.getElementById('sc-input-qty-' + productId);
    if (!input) return;

    let currentQty = parseInt(input.value);
    let newQty = currentQty + change;
    if (newQty < 1) return;

    // 1. ACTUALIZACIÓN OPTIMISTA (En el lateral)
    input.value = newQty;

    // 2. 🚀 SINCRONIZACIÓN CON LA TARJETA (El camino de vuelta)
    // Buscamos los spans de cantidad en el Home/Tienda
    const qtySpan = document.getElementById('card-qty-val-' + productId);
    const qtySpanFeature = document.getElementById('card-qty-val-feature-' + productId);

    if (qtySpan) qtySpan.innerText = newQty;
    if (qtySpanFeature) qtySpanFeature.innerText = newQty;

    if (btnElement) btnElement.disabled = true;

    try {
        const response = await fetch('/cart/api/update-quantity', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ 'productId': productId, 'delta': change })
        });

        if (response.ok) {
            // Refrescamos totales (precios y badge)
            await refreshSideCart();

            // Si tenés la función de calcular el total a mano, la llamamos
            if (typeof calcularTotalCarrito === "function") {
                calcularTotalCarrito();
            }
        }
    } catch (error) {
        console.error("Error:", error);
        // Revertimos en ambos lados si falla
        input.value = currentQty;
        if (qtySpan) qtySpan.innerText = currentQty;
        if (qtySpanFeature) qtySpanFeature.innerText = currentQty;
    } finally {
        if (btnElement) btnElement.disabled = false;
    }
}

function recalcularTotalDesdeData() {
    let acumulado = 0;
    // Sumamos todos los data-price que hay en el carrito
    document.querySelectorAll('[data-price]').forEach(el => {
        acumulado += parseFloat(el.getAttribute('data-price')) || 0;
    });

    const totalDisplay = document.getElementById('side-cart-total');
    if (totalDisplay) {
        totalDisplay.innerText = '$' + acumulado.toLocaleString('es-AR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }
}

// 3. ELIMINAR ITEM (Desde el tachito de basura del carrito)
async function removeSideItem(productId) {
    if (!confirm("¿Quitar producto del carrito?")) return;

    const row = document.getElementById('sc-row-' + productId);
    if(row) row.style.opacity = '0.5';

    try {
        // Usamos remove-ajax para obtener la tarjeta limpia y resetear el botón del home
        let response = await fetch('/cart/remove-ajax/' + productId);

        if (response.ok) {
            let htmlFragment = await response.text();

            // Restauramos las tarjetas en el Home (Nuevos y Destacados)
            const cardMain = document.getElementById('card-producto-' + productId);
            if (cardMain) cardMain.outerHTML = htmlFragment;

            const cardFeature = document.getElementById('card-producto-feature-' + productId);
            if (cardFeature) cardFeature.outerHTML = htmlFragment;

            // Actualizamos el carrito lateral
            await refreshSideCart();
        }
    } catch (e) {
        console.error("Error crítico:", e);
    }
}

async function updateCardQty(productId, change) {
    const qtySpan = document.getElementById('card-qty-val-' + productId);
    // 🚀 Buscamos también el input del carrito lateral
    const sideInput = document.getElementById('sc-input-qty-' + productId);

    if (!qtySpan) return;

    let currentQty = parseInt(qtySpan.innerText);
    let newQty = currentQty + change;

    if (newQty < 1) {
        borrarItemYRecargarTarjeta(productId);
        return;
    }

    // 1. Actualización visual instantánea (En la tarjeta)
    qtySpan.innerText = newQty;

    // 2. 🚀 SINCRONIZACIÓN DIRECTA: Si el producto está en el lateral, actualizamos su número ya mismo
    if (sideInput) {
        sideInput.value = newQty;
    }

    try {
        const response = await fetch('/cart/api/update-quantity', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ 'productId': productId, 'delta': change })
        });

        if (response.ok) {
            // 3. Refrescamos totales y precios de fila (la función "quirúrgica" que ya tenés)
            await refreshSideCart();

            // 4. Si usás la función de calcular el total a mano, la llamamos
            if (typeof calcularTotalCarrito === "function") {
                calcularTotalCarrito();
            }
        }
    } catch (e) {
        console.error("Error:", e);
        // Revertimos si algo sale mal
        qtySpan.innerText = currentQty;
        if (sideInput) sideInput.value = currentQty;
    }
}

async function refreshSideCart() {
    try {
        let response = await fetch('/cart/panel-fragment?t=' + Date.now());
        if (!response.ok) return;

        let htmlFragment = await response.text();
        let parser = new DOMParser();
        let doc = parser.parseFromString(htmlFragment, 'text/html');
        let newContent = doc.getElementById('sideCartPanel');
        const oldCart = document.getElementById('sideCartPanel');

        if (newContent && oldCart) {
            const newItems = newContent.querySelectorAll('.sc-item');
            const currentItems = oldCart.querySelectorAll('.sc-item');

            // 🚀 SI LA CANTIDAD DE PRODUCTOS ES IGUAL: Actualización quirúrgica (SIN PARPADEO)
            if (newItems.length === currentItems.length) {
                // 1. Actualizar el Total General
                const newTotal = newContent.querySelector('#side-cart-total');
                const currentTotal = oldCart.querySelector('#side-cart-total');
                if (newTotal && currentTotal) currentTotal.innerText = newTotal.innerText;

                // 2. Actualizar los Subtotales de cada fila sin tocar imágenes
                newItems.forEach(newItem => {
                    const id = newItem.id; // Ej: sc-row-123
                    const currentPrice = oldCart.querySelector(`#${id} .item-price-side`);
                    const newPrice = newItem.querySelector('.item-price-side');
                    if (currentPrice && newPrice) currentPrice.innerText = newPrice.innerText;
                });

                calcularTotalCarrito();
            } else {
                // 🚀 SI SE AGREGÓ O ELIMINÓ UN PRODUCTO: Reemplazo total (Aquí el parpadeo es necesario para mostrar/quitar la fila)
                const wasActive = oldCart.classList.contains('active');
                oldCart.innerHTML = newContent.innerHTML;
                if (wasActive) oldCart.classList.add('active');

                calcularTotalCarrito();
            }

            updateHeaderBadgeCount();
        }
    } catch (e) {
        console.error("Error refrescando carrito:", e);
    }
}


// 7. FUNCIONES AUXILIARES
function syncMainPageButton(productId, change) {
    const qtySpan = document.getElementById('card-qty-val-' + productId);
    const qtySpanFeature = document.getElementById('card-qty-val-feature-' + productId);

    if (qtySpan) {
        let current = parseInt(qtySpan.innerText);
        if (current + change >= 1) qtySpan.innerText = current + change;
    }
    if (qtySpanFeature) {
        let current = parseInt(qtySpanFeature.innerText);
        if (current + change >= 1) qtySpanFeature.innerText = current + change;
    }
}
function calcularTotalCarrito() {
    let total = 0;
    // Buscamos los precios tanto en el lateral como en la página principal
    const precios = document.querySelectorAll('.item-price-side, .item-price');

    precios.forEach(el => {
        // 1. Limpiamos el texto: solo dejamos números, puntos y comas
        let texto = el.innerText.replace('$', '').trim();

        // 2. Lógica inteligente para el separador:
        // Si hay coma y punto (ej: 1.200,50), quitamos el punto y cambiamos coma por punto
        if (texto.includes(',') && texto.includes('.')) {
            texto = texto.replace(/\./g, '').replace(',', '.');
        }
        // Si solo hay coma (ej: 660,00), la pasamos a punto para parseFloat
        else if (texto.includes(',')) {
            texto = texto.replace(',', '.');
        }
        // Si es 660.00 (como en tu foto), parseFloat lo entiende directo.

        total += parseFloat(texto) || 0;
    });

    // 3. Formateamos para mostrar en pantalla ($ 1.234,56)
    const totalFormateado = '$' + total.toLocaleString('es-AR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    // 4. Actualizamos todos los displays que existan
    const ids = ['side-cart-total', 'main-total-display', 'main-final-total', 'cart-total-display'];
    ids.forEach(id => {
        const elemento = document.getElementById(id);
        if (elemento) elemento.innerText = totalFormateado;
    });
}


function updateHeaderBadgeCount() {
    // 1. Contamos cuántos productos DISTINTOS hay (filas en el carrito lateral)
    const distinctItems = document.querySelectorAll('.sc-item').length;

    // 2. Buscamos TODOS los badges (Normal y Sticky)
    const badges = document.querySelectorAll('.badge-cart');

    badges.forEach(badge => {
        badge.innerText = distinctItems;

        // Efecto visual opcional para que se note el cambio
        badge.style.transform = "scale(1.3)";
        setTimeout(() => badge.style.transform = "scale(1)", 200);
    });
}

async function toggleFavorite(productId) {
    try {
        const response = await fetch('/toggle-favorite/' + productId);

        if (response.ok) {
            const htmlFragment = await response.text();

            // Reemplazamos todos los botones que coincidan con ese ID de producto
            // (Útil si el producto aparece en varios lugares)
            const favButtons = document.querySelectorAll('#btn-fav-' + productId);
            favButtons.forEach(btn => {
                btn.outerHTML = htmlFragment;
            });
        }
    } catch (error) {
        console.error("Error al actualizar favorito:", error);
    }
}

// EN cart-logic.js

function agregarConAjax(idProducto) {
    console.log("🚀 Agregando producto:", idProducto);

    fetch('/cart/add-ajax/' + idProducto)
        .then(response => {
            if (response.ok) return response.text();
            throw new Error('Error en el servidor');
        })
        .then(htmlFragment => {
            // 1. Reemplazamos los botones en la tarjeta
            const contenedor = document.getElementById('acciones-cart-' + idProducto);
            if (contenedor) {
                contenedor.outerHTML = htmlFragment;
            }

            // 2. Refrescamos el contenido del carrito lateral
            // Pasamos 'true' para que la función intente abrirlo
            refreshSideCart(true);

            // 3. REFUERZO: Si por alguna razón no abrió, forzamos las clases
            // Esto asegura que el panel y el fondo oscuro aparezcan sí o sí
            setTimeout(() => {
                const sideCart = document.getElementById('sideCartPanel');
                const overlay = document.querySelector('.cart-overlay');
                if (sideCart) sideCart.classList.add('active');
                if (overlay) overlay.classList.add('active');
            }, 100); // Pequeño delay para dejar que el DOM se asiente
        })
        .catch(error => console.error("❌ Error al agregar:", error));
}

// Ejemplo de función para borrar y recargar la tarjeta completa
function borrarItemYRecargarTarjeta(idProducto) {

    fetch('/cart/remove-ajax/' + idProducto)
        .then(res => res.text())
        .then(html => {
            // 1. Buscamos la TARJETA COMPLETA (La Caja Grande)
            const tarjetaEntera = document.getElementById('card-producto-' + idProducto);

            if (tarjetaEntera) {
                // 2. La reemplazamos totalmente por la nueva que viene limpia del servidor
                tarjetaEntera.outerHTML = html;

                console.log("Tarjeta " + idProducto + " reiniciada correctamente.");
            }

            // 3. Actualizamos el carrito lateral también
            refreshSideCart(false); // false = no hace falta abrirlo si ya estaba abierto
        })
        .catch(err => console.error("Error recargando tarjeta:", err));
}

// Ejecutar el cálculo apenas el navegador termine de cargar el HTML
document.addEventListener('DOMContentLoaded', () => {
    calcularTotalCarrito();
    // También actualizamos el badge de la pelotita naranja por si las moscas
    if (typeof updateHeaderBadgeCount === "function") {
        updateHeaderBadgeCount();
    }
});