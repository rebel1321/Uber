import React from 'react'

const LocationSearchPanel = (props) => {



  // Sample array of locations
  const locations = [
    "24B, Near Kapoor's Cafe, SAGE University Bhopal, Madhya Pradesh",
    "12A, Opposite City Mall, MP Nagar, Bhopal, Madhya Pradesh",
    "56, Lake View Road, Upper Lake, Bhopal, Madhya Pradesh",
    "101, Near Habibganj Railway Station, Bhopal, Madhya Pradesh",
    "7C, Ashoka Garden, Bhopal, Madhya Pradesh",
    "33, New Market, TT Nagar, Bhopal, Madhya Pradesh"
  ];
  return (
    <div className=''>
      {/* this is a sample data */}

      {
        locations.map(function(elem, idx){
          return <div
            key={idx}
            onClick={()=>{
              props.setVehiclePanelOpen(true)
              props.setPanelOpen(false)
            }}
            className='flex gap-4 border-2 p-3 border-gray-50 active:border-black rounded-xl  items-center my-2 justify-start'
          >
            <h2 className='bg-[#eee] h-10 flex items-center justify-center w-12  rounded-full'><i className="ri-map-pin-fill"></i></h2>
            <h4 className='font-medium'>{elem}</h4>
          </div>
        })
      }
      
      
    </div>
  )
}

export default LocationSearchPanel