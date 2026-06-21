import React, { useEffect } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'

export const UserLogout = () => {

    const navigate = useNavigate()

    useEffect(() => {
        const token = localStorage.getItem('userToken')

        if (!token) {
            navigate('/login')
            return
        }

        axios.get(`${import.meta.env.VITE_BASE_URL}/user/logout`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }).catch((err) => {
            console.error('Logout failed:', err.response?.data || err.message)
        }).finally(() => {
            localStorage.removeItem('userToken')
            navigate('/login')
        })
    }, [navigate])

    return (
        <div>Logging out...</div>
    )
}

export default UserLogout
