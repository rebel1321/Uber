import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

const customIcon = new L.Icon({
    iconUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
});

const ChangeView = ({ center }) => {
    const map = useMap();
    map.setView(center, 15);
    return null;
};

const LiveTracking = () => {
    const [position, setPosition] = useState({ lat: 51.505, lng: -0.09 });

    useEffect(() => {
        navigator.geolocation.getCurrentPosition((pos) => {
            setPosition({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        });

        const watchId = navigator.geolocation.watchPosition((pos) => {
            setPosition({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        });

        return () => navigator.geolocation.clearWatch(watchId);
    }, []);

    return (
        <MapContainer center={position} zoom={15} style={{ height: '100%', width: '100%' }}>
            <ChangeView center={position} />
            <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <Marker position={position} icon={customIcon} />
        </MapContainer>
    );
};

export default LiveTracking;
