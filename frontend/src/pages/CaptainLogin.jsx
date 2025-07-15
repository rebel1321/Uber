import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCaptain } from '../context/CaptainContext'
import axios from 'axios'

const CaptainLogin = () => {
  const [email,setEmail] = useState('')
    const [password,setPassword] = useState('')
  
    const {captain,setCaptain} =useCaptain();
    const navigate = useNavigate()
    const submitHandler = async (e) => {
    e.preventDefault();
    const captain = {
      email: email,
      password
    }

    const response = await axios.post(`${import.meta.env.VITE_BASE_URL}/captain/login`, captain)

    if (response.status === 200) {
      const data = response.data

      setCaptain(data.captain)
      localStorage.setItem('token', data.token)
      navigate('/captain-home')

    }

    setEmail('')
    setPassword('')
  }
  return (
    <div>
      <div className="p-7 h-screen flex flex-col justify-between">
      <div>
        <img className='w-16 mb-10' src="/uber-driver.svg" alt="" />

      <form onSubmit={(e)=>{
        submitHandler(e)
      }}>
        <h3 className="text-lg font-medium mb-2">What's your email</h3>
        <input className="bg-[#eeeeee] mb-7 rounded px-4 py-2 border w-full text-lg placeholder:text-base" 
        required 
        value={email}
        onChange={(e)=>{
          setEmail(e.target.value)
        }}
        type="email"
        autoComplete="current-password"
         placeholder="email@example.com" />
        <h3 className="text-lg font-medium mb-2">Enter Password</h3>
        <input className="bg-[#eeeeee] mb-7 rounded px-4 py-2 border w-full text-lg placeholder:text-base" 
        required 
        value={password}
        onChange={(e)=>{
          setPassword(e.target.value)
        }}
        autoComplete="current-password"
        type="password" placeholder="password" />
        <button
        className="bg-[#111] text-white font-semibold mb-3 rounded px-4 py-2  w-full text-lg placeholder:text-base" 
        >Login</button>

        <p className="text-center">New to Uber Drive?{" "}<Link to="/captain-signup" className="text-blue-600">Sign up as a Captain</Link></p>
      </form>
      </div>
      <div>
        <Link
        to="/login"
        className="bg-[#d5622d] flex items-center justify-center text-white font-semibold mb-5 rounded px-4 py-2  w-full" 
        >Sign in as User</Link>
      </div>
    </div>
    </div>
  )
}

export default CaptainLogin