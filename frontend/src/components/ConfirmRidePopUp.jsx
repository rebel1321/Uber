import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const ConfirmRidePopUp = (props) => {
  const [otp, setOtp] = useState('');
  const navigate = useNavigate();
  const distanceKm = props.ride?.distance
  const distanceLabel = typeof distanceKm === "number" ? `${distanceKm.toFixed(1)} KM` : "-- KM"

  const submitHandler = async (e) => {
    e.preventDefault();

    try {
      const response = await axios.get(`${import.meta.env.VITE_BASE_URL}/rides/start-ride`, {
        params: {
          rideId: props.ride._id,
          otp: otp
        },
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });

      if (response.status === 200) {
        props.setConfirmRidePopupPanel(false);
        props.setRidePopupPanel(false);

        // Navigate with ride data
        navigate('/captain-riding', { state: { ride: props.ride } });
      }
    } catch (err) {
      console.error('Failed to start ride:', err);
      alert('Failed to start ride. Please check OTP or try again.');
    }
  };

  return (
    <div>
      <h5 className='p-1 text-center w-[93%] absolute top-0' onClick={() => {
        props.setRidePopupPanel(false);
      }}>
        <i className="text-3xl text-gray-200 ri-arrow-down-wide-line"></i>
      </h5>

      <h3 className='text-2xl font-semibold mb-5'>Confirm this ride to Start</h3>

      <div className='flex items-center justify-between p-3 border-2 border-yellow-400 rounded-lg mt-4'>
        <div className='flex items-center gap-3'>
          <img className='h-12 rounded-full object-cover w-12' src="/UserProf.png" alt="" />
          <h2 className='text-lg font-medium capitalize'>{props.ride?.user.fullName.firstName}</h2>
        </div>
        <h5 className='text-lg font-semibold'>{distanceLabel}</h5>
      </div>

      <div className='flex gap-2 justify-between flex-col items-center'>
        <div className='w-full mt-5'>
          <div className='flex items-center gap-5 p-3 border-b-2'>
            <i className="ri-map-pin-user-fill"></i>
            <div>
              <h3 className='text-lg font-medium'>562/11-A</h3>
              <p className='text-sm -mt-1 text-gray-600'>{props.ride?.pickup}</p>
            </div>
          </div>

          <div className='flex items-center gap-5 p-3 border-b-2'>
            <i className="text-lg ri-map-pin-2-fill"></i>
            <div>
              <h3 className='text-lg font-medium'>562/11-A</h3>
              <p className='text-sm -mt-1 text-gray-600'>{props.ride?.destination}</p>
            </div>
          </div>

          <div className='flex items-center gap-5 p-3'>
            <i className="ri-currency-line"></i>
            <div>
              <h3 className='text-lg font-medium'>₹{props.ride?.fare}</h3>
              <p className='text-sm -mt-1 text-gray-600'>Cash</p>
            </div>
          </div>
        </div>

        <div className='mt-6 w-full'>
          <form onSubmit={submitHandler}>
            <input
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              type="text"
              className='bg-[#eee] px-6 py-4 font-mono text-lg rounded-lg w-full mt-3'
              placeholder='Enter OTP'
              required
            />

            {/* ✅ Replace <Link> with real submit button */}
            <button
              type="submit"
              className='w-full mt-5 text-lg flex justify-center bg-green-600 text-white font-semibold p-3 rounded-lg'
            >
              Confirm
            </button>

            <button
              type="button"
              onClick={() => {
                props.setConfirmRidePopupPanel(false);
                props.setRidePopupPanel(false);
              }}
              className='w-full mt-2 bg-red-600 text-lg text-white font-semibold p-3 rounded-lg'
            >
              Cancel
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ConfirmRidePopUp;
