import React, { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import LiveTracking from '../components/LiveTracking'
import { useSocket } from "../context/SocketContext";
import axios from "axios";

const Riding = () => {
    const location = useLocation()
    const { ride } = location.state || {} // Retrieve ride data
    const [activeRide, setActiveRide] = useState(ride || null)
    const [isPaying, setIsPaying] = useState(false)
    const navigate = useNavigate()
    const { isConnected, on } = useSocket()

    useEffect(() => {
        if (!isConnected) {
            return undefined
        }

        const offRideEnded = on("ride-ended", (rideData) => {
            setActiveRide(rideData)
        })

        return () => {
            offRideEnded()
        }
    }, [isConnected, navigate, on])
    const vehicleType = activeRide?.captain?.vehicle?.vehicleType || activeRide?.vehicleType || "car"
    const vehicleImages = {
        car: "/car.png",
        auto: "/auto.png",
        moto: "/motor.png",
    }
    const vehicleLabelMap = {
        car: "Car",
        auto: "Auto",
        moto: "Moto",
    }
    const vehicleImage = vehicleImages[vehicleType] || "/car.png"
    const vehicleLabel = vehicleLabelMap[vehicleType] || "Car"

    const isPaid = activeRide?.paid === true
    const canPay = Boolean(activeRide) && !isPaid

    const handlePayment = async () => {
        if (!activeRide?._id || !canPay) {
            return
        }
        if (activeRide?.status !== "completed") {
            alert("Ride is not completed yet.")
            return
        }
        setIsPaying(true)
        try {
            const response = await axios.post(
                `${import.meta.env.VITE_BASE_URL}/rides/pay`,
                { rideId: activeRide._id },
                {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                }
            )
            if (response.status === 200) {
                setActiveRide((prev) => prev ? { ...prev, paid: true } : prev)
                navigate('/home')
            }
        } finally {
            setIsPaying(false)
        }
    }

  return (
    <div className='h-screen'>
            <Link to='/home' className='fixed right-2 top-2 h-10 w-10 bg-white flex items-center justify-center rounded-full'>
                <i className="text-lg font-medium ri-home-5-line"></i>
            </Link>
            <div className='h-1/2 overflow-hidden'>
            <LiveTracking/>
                {/* <img src="/userHome.gif" alt="" /> */}

            </div>
            <div className='h-1/2 p-4'>
                <div className='flex items-center justify-between'>
                    <img className='h-12' src={vehicleImage} alt={vehicleLabel} />
                    <div className='text-right'>
                        <h2 className='text-lg font-medium capitalize'>
                            {activeRide?.captain?.fullName?.firstName} {activeRide?.captain?.fullName?.lastName}
                        </h2>
                        <h4 className='text-xl font-semibold -mt-1 -mb-1'>{activeRide?.captain?.vehicle?.plate || ""}</h4>
                        <p className='text-sm text-gray-600'>
                            {vehicleLabel} {activeRide?.captain?.vehicle?.color ? `- ${activeRide?.captain?.vehicle?.color}` : ""}
                        </p>

                    </div>
                </div>

                <div className='flex gap-2 justify-between flex-col items-center'>
                    <div className='w-full mt-5'>

                        <div className='flex items-center gap-5 p-3 border-b-2'>
                            <i className="text-lg ri-map-pin-2-fill"></i>
                            <div>
                                <h3 className='text-lg font-medium'>562/11-A</h3>
                                <p className='text-sm -mt-1 text-gray-600'>{activeRide?.destination}</p>
                            </div>
                        </div>
                        <div className='flex items-center gap-5 p-3'>
                            <i className="ri-currency-line"></i>
                            <div>
                                <h3 className='text-lg font-medium'>₹{activeRide?.fare}</h3>
                                <p className='text-sm -mt-1 text-gray-600'>Cash Cash</p>
                            </div>
                        </div>
                    </div>
                </div>
                <button
                    onClick={handlePayment}
                    disabled={!canPay || isPaying}
                    className='w-full mt-5 bg-green-600 text-white font-semibold p-2 rounded-lg disabled:opacity-60'
                >
                    {isPaid
                        ? "Paid"
                        : isPaying
                        ? "Processing..."
                        : activeRide?.status === "completed"
                        ? "Make a Payment"
                        : "Waiting for completion"}
                </button>
            </div>
        </div>
  )
}

export default Riding