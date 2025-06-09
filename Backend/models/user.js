const mongoose = require('mongoose');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');

const userSchema = new mongoose.Schema({
  fullName: {
    firstName: {
      type: String,
      required:true,
      minlength:[3,'First name must be at least 3 characters long'],
    },
    lastName: {
      type: String,
      minlength:[3,'Last name must be at least 3 characters long'],
    }
  },
  email:{
    type: String,
    required: true,
    unique: true,
    match: [/.+\@.+\..+/, 'Please enter a valid email address'] ,
  },
  password:{
    type: String,
    required: true,
    select:false,
    
  },
  socketId:{
    type:String,
  },


})
userSchema.methods.generateAuthToken = function () {
  const token = jwt.sign({ _id: this._id }, process.env.JWT_SECRET, { expiresIn: '1h' });
  return token;
};
userSchema.methods.comparePassword = async function (password) {  
  return await bcrypt.compare(password, this.password);
}
userSchema.statics.hashPassword = async function (password) {
  return await bcrypt.hash(password,10);
  
}

const User = mongoose.model('user',userSchema);
module.exports=User;