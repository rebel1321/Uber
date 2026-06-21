import React, { useEffect, useRef, useState } from "react";
import { useSocket } from "../context/SocketContext";
import { useUser } from "../context/UserContext";
import axios from "axios";
import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import "remixicon/fonts/remixicon.css";
import { Link, useNavigate } from "react-router-dom";
import LocationSearchPanel from "../components/LocationSearchPanel";
import VehiclePanel from "../components/VehiclePanel";
import ConfirmRide from "../components/ConfirmRide";
import LookingForDriver from "../components/LookingForDriver";
import WaitingForDriver from "../components/WaitingForDriver";
import LiveTracking from "../components/LiveTracking";

const Home = () => {
  const [pickup, setPickup] = useState("");
  const [destination, setDestination] = useState("");
  const [panelOpen, setPanelOpen] = useState(false);
  const panelRef = useRef(null);
  const panelCloseRef = useRef(null);
  const vehiclePanelRef = useRef(null);
  const confirmRidePanelRef = useRef(null);
  const vehicleFoundRef = useRef(null);
  const waitingForDriverRef = useRef(null);
  const [vehicleFound, setVehicleFound] = useState(false);
  const [waitingForDriver, setWaitingForDriver] = useState(false);
  const [vehiclePanelOpen, setVehiclePanelOpen] = useState(false);
  const [confirmRidePanel, setConfirmRidePanel] = useState(false);
  const [pickupSuggestions, setPickupSuggestions] = useState([]);
  const [destinationSuggestions, setDestinationSuggestions] = useState([]);
  const [activeField, setActiveField] = useState(null);
  const [fare, setFare] = useState({});
  const [vehicleType, setVehicleType] = useState(null);
  const [tripType, setTripType] = useState("one_way");
  const [ride, setRide] = useState(null);

  const { isConnected, on, send } = useSocket();
  const { user } = useUser();
  const navigate = useNavigate();

  useEffect(() => {
    const userId = user?.id || user?._id;
    if (!isConnected || !userId) {
      return;
    }
    send("join", { userType: "user", userId });
  }, [isConnected, send, user]);

  useEffect(() => {
    if (!isConnected) {
      return undefined;
    }

    const offConfirmed = on("ride-confirmed", (rideData) => {
      setVehicleFound(false);
      setWaitingForDriver(true);
      setRide(rideData);
    });

    const goToRiding = (rideData) => {
      setVehicleFound(false);
      setWaitingForDriver(false);
      navigate("/riding", { state: { ride: rideData } });
    };

    const offStarted = on("ride-started", goToRiding);
    const offEnded = on("ride-ended", goToRiding);

    return () => {
      offConfirmed();
      offStarted();
      offEnded();
    };
  }, [isConnected, navigate, on]);
  

  const handlePickupChange = async (e) => {
    setPickup(e.target.value);
    try {
      const response = await axios.get(
        `${import.meta.env.VITE_BASE_URL}/maps/get-suggestions`,
        {
          params: { input: e.target.value },
          headers: {
            Authorization: `Bearer ${localStorage.getItem("userToken")}`,
          },
        }
      );
      setPickupSuggestions(response.data);
    } catch (error) {
      // handle error
      console.error("Error fetching pickup suggestions", error);
    }
  };

  const handleDestinationChange = async (e) => {
    setDestination(e.target.value);
    try {
      const response = await axios.get(
        `${import.meta.env.VITE_BASE_URL}/maps/get-suggestions`,
        {
          params: { input: e.target.value },
          headers: {
            Authorization: `Bearer ${localStorage.getItem("userToken")}`,
          },
        }
      );
      setDestinationSuggestions(response.data);
    } catch (error) {
      console.error("Error fetching destination suggestions", error);
    }
  };

  const submitHandler = (e) => {
    e.preventDefault();
  };

  useGSAP(() => {
  if (panelOpen) {
    gsap.to(panelRef.current, {
      height: "70%",
      padding: 20,
      autoAlpha: 1,
    });
    gsap.to(panelCloseRef.current, {
      autoAlpha: 1,
    });
  } else {
    gsap.to(panelRef.current, {
      height: "0%",
      padding: 0,
      autoAlpha: 0,
    });
    gsap.to(panelCloseRef.current, {
      autoAlpha: 0,
    });
  }
}, [panelOpen]);

  useGSAP(() => {
  if (vehiclePanelOpen) {
    gsap.to(vehiclePanelRef.current, {
      y: 0,
      autoAlpha: 1,
    });
  } else {
    gsap.to(vehiclePanelRef.current, {
      y: "100%",
      autoAlpha: 0,
    });
  }
}, [vehiclePanelOpen]);
useGSAP(() => {
  if (confirmRidePanel) {
    gsap.to(confirmRidePanelRef.current, {
      y: 0,
      autoAlpha: 1,
    });
  } else {
    gsap.to(confirmRidePanelRef.current, {
      y: "100%",
      autoAlpha: 0,
    });
  }
}, [confirmRidePanel]);


  useGSAP(() => {
  if (vehicleFound) {
    gsap.to(vehicleFoundRef.current, {
      y: 0,
      autoAlpha: 1,
    });
  } else {
    gsap.to(vehicleFoundRef.current, {
      y: "100%",
      autoAlpha: 0,
    });
  }
}, [vehicleFound]);

 useGSAP(() => {
  if (waitingForDriver) {
    gsap.to(waitingForDriverRef.current, {
      y: 0,
      autoAlpha: 1,
    });
  } else {
    gsap.to(waitingForDriverRef.current, {
      y: "100%",
      autoAlpha: 0,
    });
  }
}, [waitingForDriver]);

  async function findTrip() {
    setVehiclePanelOpen(true);
    setPanelOpen(false);

    const response = await axios.get(
      `${import.meta.env.VITE_BASE_URL}/rides/get-fare`,
      {
        params: { pickup, destination, tripType },
        headers: {
          Authorization: `Bearer ${localStorage.getItem("userToken")}`,
        },
      }
    );

    setFare(response.data?.fare || {});
  }

  async function createRide() {
    const response = await axios.post(
      `${import.meta.env.VITE_BASE_URL}/rides/create`,
      {
        pickup,
        destination,
        vehicleType,
        tripType,
      },
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("userToken")}`,
        },
      }
    );
  }

  return (
    <div className="h-screen relative overflow-hidden">
      {/* User Icon */}
      <img
        className="w-16 absolute left-5 top-5"
        src="/Uberuser.png"
        alt="User Icon"
      />
      <Link
        to="/user/logout"
        className="absolute right-5 top-5 h-10 w-10 bg-white flex items-center justify-center rounded-full z-20"
        aria-label="Log out"
      >
        <i className="text-lg font-medium ri-logout-box-r-line"></i>
      </Link>

      {/* Background Image */}
     <div className="absolute top-0 left-0 w-full h-full z-0">
  <LiveTracking />
</div>

      {/* Form Overlay */}
      <div className="flex flex-col justify-end h-screen absolute top-0 w-full ">
        <div className="h-[30%] p-6 bg-white relative ">
          <h5
            ref={panelCloseRef}
            onClick={() => {
              setPanelOpen(false);
            }}
            className="absolute opacity-0 top-6 right-6 text-2xl"
          >
            <i className="ri-arrow-down-wide-line"></i>
          </h5>
          <h4 className="text-2xl font-semibold">Find a trip</h4>
          <div className="mt-3 flex gap-2">
            <button
              type="button"
              onClick={() => setTripType("one_way")}
              className={`px-3 py-1 rounded-full text-sm ${tripType === "one_way" ? "bg-black text-white" : "bg-gray-200 text-gray-700"}`}
            >
              One way
            </button>
            <button
              type="button"
              onClick={() => setTripType("round_trip")}
              className={`px-3 py-1 rounded-full text-sm ${tripType === "round_trip" ? "bg-black text-white" : "bg-gray-200 text-gray-700"}`}
            >
              Round trip
            </button>
          </div>
          <form className="relative " onSubmit={submitHandler}>
  <div className="absolute left-5 top-14 h-15 w-[2px] bg-gray-700 rounded"></div>

            <input
              onClick={() => {
                setPanelOpen(true);
                setActiveField("pickup");
              }}
              value={pickup}
              onChange={handlePickupChange}
              className="bg-[#eee] px-12 py-4 text-lg rounded-lg w-full mt-5"
              type="text"
              placeholder="Add a pick-up location"
            />
            <input
              onClick={() => {
                setPanelOpen(true);
                setActiveField("destination");
              }}
              value={destination}
              onChange={handleDestinationChange}
              className="bg-[#eee] px-12 py-4 text-lg rounded-lg w-full mt-3"
              type="text"
              placeholder="Enter your destination"
            />
          </form>
          <button
            onClick={findTrip}
            className="bg-black text-white px-4 py-2 rounded-lg mt-3 w-full"
          >
            Find Trip
          </button>
        </div>
        <div ref={panelRef} className="h-0 bg-white ">
          <LocationSearchPanel
            suggestions={
              activeField === "pickup"
                ? pickupSuggestions
                : destinationSuggestions
            }
            setPanelOpen={setPanelOpen}
            setVehiclePanelOpen={setVehiclePanelOpen}
            setPickup={setPickup}
            setDestination={setDestination}
            activeField={activeField}
          />
        </div>
      </div>
      <div
        ref={vehiclePanelRef}
        className="fixed w-full z-10 bottom-0 translate-y-full bg-white px-3 py-10 pt-12"
      >
        <VehiclePanel
          fare={fare}
          selectVehicle={setVehicleType}
          setConfirmRidePanel={setConfirmRidePanel}
          setVehiclePanelOpen={setVehiclePanelOpen}
        />
      </div>
      <div
        ref={confirmRidePanelRef}
        className="fixed w-full z-10 bottom-0 translate-y-full bg-white px-3 py-10 pt-12"
      >
        <ConfirmRide
          createRide={createRide}
          pickup={pickup}
          destination={destination}
          fare={fare}
          vehicleType={vehicleType}
          setConfirmRidePanel={setConfirmRidePanel}
          setVehicleFound={setVehicleFound}
        />
      </div>
      <div
        ref={vehicleFoundRef}
        className="fixed w-full z-10 bottom-0 translate-y-full bg-white px-3 py-6 pt-12"
      >
        <LookingForDriver
                    createRide={createRide}
                    pickup={pickup}
                    destination={destination}
                    fare={fare}
                    vehicleType={vehicleType}
                    setVehicleFound={setVehicleFound} /> 
      </div>
      <div
        ref={waitingForDriverRef}
        className="fixed w-full  z-10 bottom-0 bg-white px-3 py-2 pt-15"
      >
        <WaitingForDriver
                    ride={ride}
                    setVehicleFound={setVehicleFound}
                    setWaitingForDriver={setWaitingForDriver}
                    waitingForDriver={waitingForDriver} />
      </div>
    </div>
  );
};

export default Home;
// 5:15:00
