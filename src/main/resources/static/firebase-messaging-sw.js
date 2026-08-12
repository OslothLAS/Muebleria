importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-messaging-compat.js');

const firebaseConfig = {
    apiKey: "AIzaSyCF-ZvUMVVypkIoA9P3OZksEN1W3y7s6dE",
    authDomain: "muebleria-9f9fd.firebaseapp.com",
    projectId: "muebleria-9f9fd",
    storageBucket: "muebleria-9f9fd.firebasestorage.app",
    messagingSenderId: "791274403224",
    appId: "1:791274403224:web:222503570680d272ba27ed",
    measurementId: "G-2V704WP99D"
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

// 2. Lógica para manejar notificaciones en segundo plano
messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] Notificación en segundo plano recibida ', payload);

    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: '/assets/logo/favicon.png' // Asegurate de que la ruta a tu logo sea correcta
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});