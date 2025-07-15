const rideModel = require('../models/rideModel');
const mapService = require('./mapsService');
const crypto = require('crypto');
async function getFare(pickup, destination) {
  if (!pickup || !destination) {
    throw new Error('Pickup and destination are required to calculate fare');
  }

  const distanceTime = await mapService.getDistanceAndTime(pickup, destination);

  const distanceInKm = distanceTime.distance / 1000;
  const timeInMinutes = distanceTime.duration / 60;
  
  const baseFare = { auto: 20, car: 30, moto: 10 };
  const perKmRate = { auto: 5, car: 8, moto: 3 };
  const perMinRate = { auto: 1, car: 2, moto: 0.5 };

  const fares = {};

  for (const type of Object.keys(baseFare)) {
    const fare =
      baseFare[type] +
      perKmRate[type] * distanceInKm +
      perMinRate[type] * timeInMinutes;

    fares[type] = Math.round(fare); // optional rounding
  }
  
  return fares;
}

module.exports.getFare = getFare;

function getOtp(num){
  function generateOtp(num){
    const otp = crypto.randomInt(Math.pow(10, num-1),Math.pow(10,num)).toString();
    return otp;
  }
  return generateOtp(num);
}
module.exports.createRide = async ({
  user, pickup, destination, vehicleType
}) => {
  if (!user || !pickup || !destination || !vehicleType) {
    throw new Error('User, pickup, destination, and vehicle type are required to create a ride');
  }

  const fare = await getFare(pickup, destination);
  const ride = rideModel.create({
    user,
    pickup,
    destination,
    otp: getOtp(6),
    fare: fare[vehicleType],
  });

  return ride;
};
module.exports.confirmRide = async ({
    rideId, captain
}) => {
    if (!rideId) {
        throw new Error('Ride id is required');
    }

    await rideModel.findOneAndUpdate({
        _id: rideId
    }, {
        status: 'accepted',
        captain: captain._id
    })

    const ride = await rideModel.findOne({
        _id: rideId
    }).populate('user').populate('captain').select('+otp');

    if (!ride) {
        throw new Error('Ride not found');
    }

    return ride;

}

module.exports.startRide = async ({ rideId, otp, captain }) => {
    if (!rideId || !otp) {
        throw new Error('Ride id and OTP are required');
    }

    const ride = await rideModel.findOne({
        _id: rideId
    }).populate('user').populate('captain').select('+otp');

    if (!ride) {
        throw new Error('Ride not found');
    }

    if (ride.status !== 'accepted') {
        throw new Error('Ride not accepted');
    }

    if (ride.otp !== otp) {
        throw new Error('Invalid OTP');
    }

    await rideModel.findOneAndUpdate({
        _id: rideId
    }, {
        status: 'ongoing'
    })

    return ride;
}

module.exports.endRide = async ({ rideId, captain }) => {
    if (!rideId) {
        throw new Error('Ride id is required');
    }

    const ride = await rideModel.findOne({
        _id: rideId,
        captain: captain._id
    }).populate('user').populate('captain').select('+otp');

    if (!ride) {
        throw new Error('Ride not found');
    }

    if (ride.status !== 'ongoing') {
        throw new Error('Ride not ongoing');
    }

    await rideModel.findOneAndUpdate({
        _id: rideId
    }, {
        status: 'completed'
    })

    return ride;
}

