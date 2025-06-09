const express = require('express');
const router = express.Router();
const {body} =require("express-validator")
const userController = require('../controllers/userController');
router.post('/register', [
    body('fullName.firstName').isLength({ min: 3 }).withMessage('FirstName must be at least 3 characters long'),
    body('email').isEmail().withMessage('Invalid email address'),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters long'),
], userController.registerUser);  

module.exports = router;