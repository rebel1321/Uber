import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'

const CaptainLogout = () => {
  const navigate = useNavigate()

  useEffect(() => {
    const token = localStorage.getItem('token')

    if (!token) {
      // Token is missing or invalid
      navigate('/captain-login')
      return
    }

    axios.get(`${import.meta.env.VITE_BASE_URL}/captain/logout`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((response) => {
        if (response.status === 200) {
          localStorage.removeItem('token')
          navigate('/captain-login')
        }
      })
      .catch((err) => {
        console.error('Logout failed:', err.response?.data || err.message)

        // Still remove token and redirect to login (force logout)
        localStorage.removeItem('token')
        navigate('/captain-login')
      })
  }, [navigate])

  return <div>Logging out...</div>
}

export default CaptainLogout
