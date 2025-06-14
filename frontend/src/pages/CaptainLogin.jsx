import React, { useState } from 'react'
import { Link } from 'react-router-dom'

const CaptainLogin = () => {
  const [email,setEmail] = useState('')
    const [password,setPassword] = useState('')
    const [captainData,setCaptainData] = useState({})
  
    const submitHandler =(e)=>{
      e.preventDefault();
      setCaptainData({
        email:email,
        password:password
      })
      console.log(captainData)
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
         placeholder="email@example.com" />
        <h3 className="text-lg font-medium mb-2">Enter Password</h3>
        <input className="bg-[#eeeeee] mb-7 rounded px-4 py-2 border w-full text-lg placeholder:text-base" 
        required 
        value={password}
        onChange={(e)=>{
          setPassword(e.target.value)
        }}
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