const Captain = require('../models/captainModel')

module.exports.createCaptain = async ({
  firstName,lastName,email,password,color,model,plate,capacity,vehicleType
})=>{
  if(!firstName || !email || !password || !color|| !plate||!capacity||!vehicleType){
    throw new Error('All fields are required');
  }
  try {
    const captain = await Captain.create({
      fullName: {
        firstName,
        lastName
      },
      email,
      password,
      vehicle: {
        color,
        plate,
        capacity,
        vehicleType
      }
    });
  
    return captain;
  }catch (error) {
  console.error('MongoDB Error while creating captain:', error); // 👈 THIS LINE is critical
  throw new Error('Error creating captain');
}
}
