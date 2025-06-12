const User = require("../models/user");
const jwt = require("jsonwebtoken");  
const bcrypt = require("bcrypt");
const blacklistToken = require("../models/blacklistToken");
const Captain = require("../models/captainModel");

module.exports.authUser = async (req, res, next) => {
  const token = req.cookies.token || req.headers.authorization?.split(" ")[1];
  if (!token) {
    return res.status(401).json({ message: "Authentication token is missing" });
  }

  const isBlackListed = await User.findOne({token:token});

  if(isBlackListed){
    return res.status(401).json({ message: "Token is blacklisted" });
  }
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    const user = await User.findById(decoded._id);
    if (!user) {
      return res.status(401).json({ message: "Invalid token" });
    }
    req.user = user;
    return next();
  } catch (error) {
    console.error("Authentication error:", error);
    return res.status(401).json({ message: "Invalid token" });
  } 
}


module.exports.authCaptain = async (req, res, next) => {
  const token = req.cookies.token || req.headers.authorization?.split(" ")[1];
  if (!token) {
    return res.status(401).json({ message: "Authentication token is missing" });
  }

  const isBlackListed = await blacklistToken.findOne({token:token});

  if(isBlackListed){
    return res.status(401).json({ message: "Token is blacklisted" });
  }
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    const captain = await Captain.findById(decoded._id);
    if (!captain) {
      return res.status(401).json({ message: "Invalid token" });
    }
    req.captain = captain;
    return next();
  } catch (error) {
    console.error("Authentication error:", error);
    return res.status(401).json({ message: "Invalid token" });
  } 
}