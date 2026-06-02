/* ================================
   1. Lógica para la X del MENÚ (Header)
   ================================ */
function removeMiniCartItem(event, productId, element) {
    event.preventDefault();
    fetch('/cart/remove-api/' + productId)
        .then(response => {
            if (response.ok) {
                // Borramos la línea del menú
                const listItem = element.closest('li.single-cart-list');
                if (listItem) listItem.remove();

                // Actualizamos la bolita naranja
                updateBadgeCount(-1);

                // TRUCO: Si estamos en la página del carrito, también recargamos para que se vea el cambio en la tabla
                if (window.location.pathname === '/cart') {
                    location.reload();
                }
            }
        });
}

/* ================================
   2. Lógica para la X de la TABLA ROJA (Cart Page)
   ================================ */
function removeCartPageItem(event, productId, buttonElement) {
    event.preventDefault();
    fetch('/cart/remove-api/' + productId)
        .then(response => {
            if (response.ok) {
                // Borramos la fila de la tabla
                const row = buttonElement.closest('tr');
                row.remove();

                // Actualizamos la bolita naranja
                updateBadgeCount(-1);

                // Recalcular Total (Visualmente)
                recalculateTotal(row);
            }
        });
}

/* ================================
   Auxiliares
   ================================ */
function updateBadgeCount(change) {
    const badge = document.querySelector('.badge.badge-bg-1');
    if (badge) {
        let count = parseInt(badge.innerText);
        let newCount = count + change;
        if (newCount < 0) newCount = 0;
        badge.innerText = newCount;
    }
}

function recalculateTotal(rowRemoved) {
    // Buscamos el subtotal que acabamos de borrar para restarlo
    const subtotalCell = rowRemoved.querySelectorAll('td')[4];
    const totalElement = document.querySelector('strong[style*="font-size: 20px"]');

    if (subtotalCell && totalElement) {
        let subtotal = parseFloat(subtotalCell.innerText.replace('$', '').trim());
        let currentTotal = parseFloat(totalElement.innerText.replace('$', '').trim());
        let newTotal = currentTotal - subtotal;
        totalElement.innerText = '$ ' + newTotal.toFixed(2);
    }

    /* ================================
   LOGICA DE CANTIDAD (Corregida)
   ================================ */

// 1. Función para los botones + y -
    function operarCantidad(productId, cambio, boton) {
        // A. Encontrar el contenedor padre usando la clase específica
        var contenedor = boton.closest('.control-cantidad');

        // B. Buscar el input DENTRO de ese contenedor
        var input = contenedor.querySelector('.input-qty');

        // C. Calcular nuevo valor
        var valorActual = parseInt(input.value) || 0;
        var nuevoValor = valorActual + cambio;

        // D. REGLA DE ORO: Nunca menos de 1
        if (nuevoValor < 1) {
            nuevoValor = 1;
        }

        // E. Actualizar visualmente y avisar al servidor
        input.value = nuevoValor;
        actualizarServidor(productId, nuevoValor, input);
    }

// 2. Función para cuando escribes a mano (Validador)
    function validarManual(input) {
        var valor = parseInt(input.value);
        var productId = input.getAttribute('data-id');

        // Si está vacío o es menor a 1, lo forzamos a 1
        if (isNaN(valor) || valor < 1) {
            input.value = 1;
            valor = 1;
        }

        actualizarServidor(productId, valor, input);
    }

// 3. Lógica que habla con el Backend (Reutilizada)
    function actualizarServidor(productId, quantity, inputElement) {
        fetch('/cart/update-item', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: 'productId=' + productId + '&quantity=' + quantity
        })
            .then(response => {
                if (response.ok) {
                    // Recalcular precios visualmente
                    recalculateRowAndTotal(inputElement, quantity);
                    updateHeaderBadge();
                }
            });
    }

// (Asegúrate de mantener las funciones 'recalculateRowAndTotal' y 'updateHeaderBadge' que ya tenías)
}