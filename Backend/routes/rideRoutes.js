const express = require('express');
const router = express.Router();
const rideController = require('../controllers/rideController');
const {body,query} = require("express-validator");
const authMiddleware = require('../middleware/authMiddleware');


router.post('/create',
  authMiddleware.authUser,
  body('pickup').isString().isLength({min: 3}).withMessage('Invalid pickup location'),
  body('destination').isString().isLength({min: 3}).withMessage('Invalid destination location'),
  body('vehicleType').isIn(['auto', 'car', 'moto']).withMessage('Invalid vehicle type'),
  rideController.createRide
)
router.get('/get-fare',
    authMiddleware.authUser,
    query('pickup').isString().isLength({ min: 3 }).withMessage('Invalid pickup address'),
    query('destination').isString().isLength({ min: 3 }).withMessage('Invalid destination address'),
    rideController.getFare
)


module.exports = router;