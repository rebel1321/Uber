const User = require("../models/user")
const userService = require("../services/userService")
const {validationResult} = require("express-validator")
const blacklistToken = require("../models/blacklistToken");
module.exports.registerUser = async (req, res, next) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { fullName, email, password } = req.body;

    const isUserAlreadyExist = await User.findOne({email});
    if(isUserAlreadyExist){
        return res.status(400).json({message:'User already exist'});
    }
    const hashPasword = await User.hashPassword(password);
    const user = await userService.createUser({
      firstName: fullName.firstName,
      lastName: fullName.lastName,
      email,
      password: hashPasword
    });

    const token = user.generateAuthToken();
    res.status(201).json({ token, user });
  } catch (error) {
    next(error); // passes the error to Express global error handler
  }
};

module.exports.loginUser = async (req, res, next) => {
  try {
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
    res.cookie('token', token);
    res.status(200).json({ token, user });
  } catch (error) {
    next(error);
  }
};
module.exports.getUserProfile = async (req, res, next) => {
  res.status(200).json({
    user: req.user,
    message: "User profile fetched successfully"
  });
}

module.exports.logoutUser = async (req, res, next) => {
  res.clearCookie('token');
  const token = req.cookies.token || req.headers.authorization?.split(" ")[1];
  await blacklistToken.create({token});

  res.status(200).json({ message: "User logged out successfully" });
}