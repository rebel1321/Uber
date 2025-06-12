const express = require('express')
const router = express.Router();
const {body} =require("express-validator")
const captainController = require('../controllers/captainController');
const authMiddleware = require('../middleware/authMiddleware');


router.post('/register',[
  body('email').isEmail().withMessage('Invalid email address'),
  body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters long'),
  body('fullName.firstName').isLength({ min: 3 }).withMessage('First name must be at least 3 characters long'),
  body('vehicle.color').isLength({min:3}).withMessage('Color must be atleast 3 character long'),
  body('vehicle.plate').isLength({min:3}).withMessage('Plate must be atleast 3 character long'),
  body('vehicle.capacity').isLength({min:1}).withMessage('Capacity must be atleast 1 character long'),
  body('vehicle.vehicleType').isIn(['car','motorcycle','auto']).withMessage('Invalid vehicle type'),

],captainController.registerCaptain);

router.post('/login',[
  body('email').isEmail().withMessage('Invalid email address'),
  body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters long'),
],captainController.loginCaptain);

router.get('/profile', authMiddleware.authCaptain,captainController.getCaptainProfile);

router.get('/logout', authMiddleware.authCaptain, captainController.logoutCaptain);
module.exports=router;