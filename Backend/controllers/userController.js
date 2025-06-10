const User = require("../models/user")
const userService = require("../services/userService")
const {validationResult} = require("express-validator")
module.exports.registerUser = async (req, res,next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }

  const {fullName, email, password } = req.body;

  const hashPasword = await User.hashPassword(password);
  const user = await userService.createUser({
    firstName:fullName.firstName,
    lastName:fullName.lastName,
    email,
    password: hashPasword
  });
  const token = user.generateAuthToken();
  res.status(201).json({token,user});
}
module.exports.loginUser = async (req, res,next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }

  const { email, password } = req.body;
  const user = await User.findOne({ email }).select('+password');

  if (!user) {
    return res.status(401).json({ message: 'Invalid email or password' });
  }

  const isMatch = await user.comparePassword(password);
  if (!isMatch) {
    return res.status(401).json({ message: 'Invalid password' });
  }

  const token = user.generateAuthToken();
  res.status(200).json({ token, user });
}