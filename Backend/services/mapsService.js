const axios = require('axios');

const getAddressCoordinates = async (address) => {
  const apiKey = process.env.OLA_API_KEY;
  const url = 'https://api.olamaps.io/places/v1/geocode';

  try {
    const response = await axios.get(url, {
      params: {
        address: address,
        api_key: apiKey
      }
    });

    const result = response.data?.geocodingResults?.[0];

    if (!result) {
      console.log('No coordinates found for address:', address);
      return null;
    }

    const { lat, lng } = result.geometry.location;

    return {
      latitude: lat,
      longitude: lng,
      formatted_address: result.formatted_address
    };
  } catch (error) {
    console.error('Error fetching coordinates from Ola Maps:', error.response?.data || error.message);
    throw new Error('Failed to fetch coordinates');
  }
};


const getDistanceAndTime = async (originAddress, destinationAddress) => {
  const apiKey = process.env.OLA_API_KEY;
  const url = 'https://api.olamaps.io/routing/v1/distanceMatrix';

  const originCoords = await getAddressCoordinates(originAddress);
  const destinationCoords = await getAddressCoordinates(destinationAddress);

  if (!originCoords || !destinationCoords) {
    throw new Error('Could not fetch coordinates for origin or destination');
  }

  const response = await axios.get(url, {
    params: {
      origins: `${originCoords.latitude},${originCoords.longitude}`,
      destinations: `${destinationCoords.latitude},${destinationCoords.longitude}`,
      api_key: apiKey
    }
  });

  if (!response.data.rows || !response.data.rows[0].elements[0]) {
    throw new Error('No distance data found');
  }

  return response.data.rows[0].elements[0];
};
const getAutoCompleteSuggestions = async (input) => {
  if (!input || input.length < 1) {
    throw new Error('Query must be at least 3 characters long');
  }
  const apiKey = process.env.OLA_API_KEY;
  const url = 'https://api.olamaps.io/places/v1/autocomplete';

  try {
    const response = await axios.get(url, {
      params: { 
        input: input,
        api_key: apiKey
      }
    });

    return response.data?.predictions || [];
  } catch (error) {
    console.error('Error fetching autocomplete suggestions from Ola Maps:', error.response?.data || error.message);
    throw new Error('Failed to fetch autocomplete suggestions');
  }
};

module.exports = {
  getAddressCoordinates,
  getDistanceAndTime,
  getAutoCompleteSuggestions
};