import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import axios from 'axios'
import { useUser} from '../context/userContext'
const UserSignup = () => {
  const [ email, setEmail ] = useState('')
  const [ password, setPassword ] = useState('')
  const [ firstName, setFirstName ] = useState('')
  const [ lastName, setLastName ] = useState('')
  const [ userData, setUserData ] = useState({})

  const { user, setUser } = useUser()
  const navigate = useNavigate()
  const submitHandler = async (e) => {
    e.preventDefault()
    const newUser = {
      fullName: {
        firstName: firstName,
        lastName: lastName
      },
      email:email,
      password:password
    }

    const response = await axios.post(`${import.meta.env.VITE_BASE_URL}/api/user/register`, newUser)
    if(response.status === 201){
      const data = response.data
      setUser(data.user)
      localStorage.setItem('token',data.token)
      navigate('/home')
    }

    setEmail('')
    setPassword('')
    setFirstName('')
    setLastName('')
  }
  return (
    <div>
    <div className="p-7 h-screen flex flex-col justify-between">
      <div>
        <img className='w-16 mb-10' src="/Uberuser.png" alt="" />

      <form onSubmit={(e)=>{
        submitHandler(e)
      }}>

        <h3 className="text-lg font-medium mb-2">What's your name</h3>
        <div className='flex gap-4 mb-7'>
          <input className="bg-[#eeeeee] w-1/2  rounded px-4 py-2 border  text-lg placeholder:text-base" 
        required 
        
        type="text"
         placeholder="First name"
         value={firstName}
                onChange={(e) => {
                  setFirstName(e.target.value)
                }} />
         <input className="bg-[#eeeeee] w-1/2  rounded px-4 py-2 border  text-lg placeholder:text-base" 
        required 
        
        type="text"
         placeholder="Last name"
         value={lastName}
                onChange={(e) => {
                  setLastName(e.target.value)
                }} />
        </div>
        <h3 className="text-lg font-medium mb-2">What's your email</h3>
        <input className="bg-[#eeeeee] mb-7 rounded px-4 py-2 border w-full text-lg placeholder:text-base" 
        required 
        value={email}
              onChange={(e) => {
                setEmail(e.target.value)
              }}
        type="email"
         placeholder="email@example.com" />
        <h3 className="text-lg font-medium mb-2">Enter Password</h3>
        <input className="bg-[#eeeeee] mb-7 rounded px-4 py-2 border w-full text-lg placeholder:text-base" 
        required 
        
        type="password"
        value={password}
              onChange={(e) => {
                setPassword(e.target.value)
              }} placeholder="password" />
        <button
        className="bg-[#111] text-white font-semibold mb-3 rounded px-4 py-2  w-full " 
        >Create account</button>

        <p className="text-center">Already have an account? <Link to="/login" className="text-blue-600">Login here </Link></p>
      </form>
      </div>
      <div className="w-full text-center">
        <p className='text-[10px] leading-tight'>This site is protected by reCAPTCHA and the <span className='underline'>Google Privacy
            Policy</span> and <span className='underline'>Terms of Service apply</span>.</p>
      </div>
    </div>
    </div>
  )
}

export default UserSignup