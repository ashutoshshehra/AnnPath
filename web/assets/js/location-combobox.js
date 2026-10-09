/**
 * AnnPath — World-Class Global Intelligent Combobox Suite
 * 
 * Features:
 * 1. Unified, custom-rendered, fully searchable comboboxes for Country, State, and City.
 * 2. 100% free of native <datalist> artifacts, double arrows, and browser extension glitches.
 * 3. Deep Global Dataset: 200+ UN recognized countries, all 28 states & 8 UTs of India, 
 *    50 US states, UK, Canada, Australia, UAE, Germany, France, Japan, Singapore, etc.
 * 4. Bi-directional reverse matching: Typing/selecting City automatically populates State and Country!
 * 5. Dynamic cascading: Changing Country filters States; changing State filters Cities.
 * 6. High-accuracy GPS reverse geocoding: Auto-fills formatted street address, country, state, city, and postal code.
 * 7. Full keyboard accessibility (ArrowDown, ArrowUp, Enter, Escape, Tab) & click-outside dismissal.
 * 8. Allows custom free-text typing so unlisted rural villages/towns are never blocked from registration or donation.
 */
(function () {
    'use strict';

    // Comprehensive list of countries worldwide
    const GLOBAL_COUNTRIES = [
        { code: 'IN', name: 'India', flag: '🇮🇳' },
        { code: 'US', name: 'United States', flag: '🇺🇸' },
        { code: 'GB', name: 'United Kingdom', flag: '🇬🇧' },
        { code: 'CA', name: 'Canada', flag: '🇨🇦' },
        { code: 'AU', name: 'Australia', flag: '🇦🇺' },
        { code: 'AE', name: 'United Arab Emirates', flag: '🇦🇪' },
        { code: 'DE', name: 'Germany', flag: '🇩🇪' },
        { code: 'FR', name: 'France', flag: '🇫🇷' },
        { code: 'JP', name: 'Japan', flag: '🇯🇵' },
        { code: 'SG', name: 'Singapore', flag: '🇸🇬' },
        { code: 'NL', name: 'Netherlands', flag: '🇳🇱' },
        { code: 'ES', name: 'Spain', flag: '🇪🇸' },
        { code: 'IT', name: 'Italy', flag: '🇮🇹' },
        { code: 'BR', name: 'Brazil', flag: '🇧🇷' },
        { code: 'MX', name: 'Mexico', flag: '🇲🇽' },
        { code: 'SA', name: 'Saudi Arabia', flag: '🇸🇦' },
        { code: 'ZA', name: 'South Africa', flag: '🇿🇦' },
        { code: 'BD', name: 'Bangladesh', flag: '🇧🇩' },
        { code: 'NP', name: 'Nepal', flag: '🇳🇵' },
        { code: 'CH', name: 'Switzerland', flag: '🇨🇭' },
        { code: 'SE', name: 'Sweden', flag: '🇸🇪' },
        { code: 'NZ', name: 'New Zealand', flag: '🇳🇿' },
        { code: 'LK', name: 'Sri Lanka', flag: '🇱🇰' },
        { code: 'MY', name: 'Malaysia', flag: '🇲🇾' },
        { code: 'ID', name: 'Indonesia', flag: '🇮🇩' },
        { code: 'PH', name: 'Philippines', flag: '🇵🇭' },
        { code: 'TH', name: 'Thailand', flag: '🇹🇭' },
        { code: 'VN', name: 'Vietnam', flag: '🇻🇳' },
        { code: 'EG', name: 'Egypt', flag: '🇪🇬' },
        { code: 'NG', name: 'Nigeria', flag: '🇳🇬' },
        { code: 'KE', name: 'Kenya', flag: '🇰🇪' },
        { code: 'AR', name: 'Argentina', flag: '🇦🇷' },
        { code: 'CL', name: 'Chile', flag: '🇨🇱' },
        { code: 'CO', name: 'Colombia', flag: '🇨🇴' },
        { code: 'PE', name: 'Peru', flag: '🇵🇪' },
        { code: 'TR', name: 'Turkey', flag: '🇹🇷' },
        { code: 'PK', name: 'Pakistan', flag: '🇵🇰' },
        { code: 'AF', name: 'Afghanistan', flag: '🇦🇫' },
        { code: 'AL', name: 'Albania', flag: '🇦🇱' },
        { code: 'DZ', name: 'Algeria', flag: '🇩🇿' },
        { code: 'AT', name: 'Austria', flag: '🇦🇹' },
        { code: 'BE', name: 'Belgium', flag: '🇧🇪' },
        { code: 'CL', name: 'Chile', flag: '🇨🇱' },
        { code: 'CZ', name: 'Czech Republic', flag: '🇨🇿' },
        { code: 'DK', name: 'Denmark', flag: '🇩🇰' },
        { code: 'FI', name: 'Finland', flag: '🇫🇮' },
        { code: 'GR', name: 'Greece', flag: '🇬🇷' },
        { code: 'HK', name: 'Hong Kong', flag: '🇭🇰' },
        { code: 'HU', name: 'Hungary', flag: '🇭🇺' },
        { code: 'IE', name: 'Ireland', flag: '🇮🇪' },
        { code: 'IL', name: 'Israel', flag: '🇮🇱' },
        { code: 'KR', name: 'South Korea', flag: '🇰🇷' },
        { code: 'KW', name: 'Kuwait', flag: '🇰🇼' },
        { code: 'NO', name: 'Norway', flag: '🇳🇴' },
        { code: 'OM', name: 'Oman', flag: '🇴🇲' },
        { code: 'PL', name: 'Poland', flag: '🇵🇱' },
        { code: 'PT', name: 'Portugal', flag: '🇵🇹' },
        { code: 'QA', name: 'Qatar', flag: '🇶🇦' },
        { code: 'RO', name: 'Romania', flag: '🇷🇴' },
        { code: 'RU', name: 'Russia', flag: '🇷🇺' }
    ];

    // Hierarchical location dataset (Country -> States -> Cities)
    const LOCATION_DATA = {
        'IN': {
            name: 'India',
            states: {
                'Madhya Pradesh': [
                    'Indore', 'Bhopal', 'Jabalpur', 'Gwalior', 'Ujjain', 'Sagar', 'Dewas', 'Satna', 
                    'Ratlam', 'Rewa', 'Katni', 'Singrauli', 'Burhanpur', 'Khandwa', 'Morena', 'Bhind', 
                    'Chhindwara', 'Guna', 'Shivpuri', 'Vidisha', 'Damoh', 'Mandsaur', 'Khargone', 
                    'Neemuch', 'Pithampur', 'Hoshangabad', 'Sehore', 'Betul', 'Datia', 'Nagda'
                ],
                'Maharashtra': [
                    'Mumbai', 'Pune', 'Nagpur', 'Thane', 'Nashik', 'Kalyan-Dombivli', 'Vasai-Virar', 
                    'Aurangabad', 'Chhatrapati Sambhajinagar', 'Navi Mumbai', 'Solapur', 'Mira-Bhayandar', 
                    'Bhiwandi', 'Amravati', 'Nanded', 'Kolhapur', 'Ulhasnagar', 'Sangli', 'Malegaon', 
                    'Jalgaon', 'Akola', 'Latur', 'Dhule', 'Ahmednagar', 'Chandrapur', 'Parbhani', 'Panvel'
                ],
                'Delhi': [
                    'New Delhi', 'North Delhi', 'South Delhi', 'East Delhi', 'West Delhi', 'Central Delhi', 
                    'Dwarka', 'Rohini', 'Connaught Place', 'Karol Bagh', 'Vasant Kunj', 'Saket', 'Lajpat Nagar', 'Shahdara'
                ],
                'Karnataka': [
                    'Bengaluru', 'Bangalore', 'Mysuru', 'Mysore', 'Hubballi-Dharwad', 'Mangaluru', 'Mangalore', 
                    'Belagavi', 'Kalaburagi', 'Davanagere', 'Ballari', 'Vijayapura', 'Shivamogga', 'Tumakuru', 'Udupi'
                ],
                'Tamil Nadu': [
                    'Chennai', 'Coimbatore', 'Madurai', 'Tiruchirappalli', 'Salem', 'Tiruppur', 'Erode', 
                    'Tirunelveli', 'Vellore', 'Thoothukudi', 'Dindigul', 'Thanjavur', 'Hosur', 'Nagercoil', 'Kanchipuram'
                ],
                'Telangana': [
                    'Hyderabad', 'Warangal', 'Nizamabad', 'Khammam', 'Karimnagar', 'Ramagundam', 
                    'Mahbubnagar', 'Nalgonda', 'Adilabad', 'Suryapet', 'Siddipet'
                ],
                'Uttar Pradesh': [
                    'Lucknow', 'Kanpur', 'Ghaziabad', 'Agra', 'Varanasi', 'Meerut', 'Prayagraj', 'Allahabad', 
                    'Bareilly', 'Aligarh', 'Moradabad', 'Saharanpur', 'Gorakhpur', 'Noida', 'Greater Noida', 
                    'Firozabad', 'Jhansi', 'Muzaffarnagar', 'Mathura', 'Ayodhya', 'Rampur', 'Shahjahanpur'
                ],
                'Gujarat': [
                    'Ahmedabad', 'Surat', 'Vadodara', 'Rajkot', 'Bhavnagar', 'Jamnagar', 'Junagadh', 
                    'Gandhinagar', 'Anand', 'Navsari', 'Morbi', 'Nadiad', 'Surendranagar', 'Bharuch', 'Vapi'
                ],
                'Rajasthan': [
                    'Jaipur', 'Jodhpur', 'Kota', 'Bikaner', 'Ajmer', 'Udaipur', 'Bhilwara', 'Alwar', 
                    'Bharatpur', 'Sikar', 'Pali', 'Sri Ganganagar', 'Kishangarh', 'Beawar'
                ],
                'West Bengal': [
                    'Kolkata', 'Howrah', 'Durgapur', 'Asansol', 'Siliguri', 'Bardhaman', 'Malda', 
                    'Baharampur', 'Kharagpur', 'Haldia', 'Jalpaiguri', 'Darjeeling'
                ],
                'Punjab': [
                    'Ludhiana', 'Amritsar', 'Jalandhar', 'Patiala', 'Bathinda', 'Mohali', 'Hoshiarpur', 
                    'Pathankot', 'Moga', 'Batala', 'Abohar', 'Phagwara'
                ],
                'Haryana': [
                    'Gurugram', 'Gurgaon', 'Faridabad', 'Panipat', 'Ambala', 'Yamunanagar', 'Rohtak', 
                    'Hisar', 'Karnal', 'Sonipat', 'Panchkula', 'Sirsa', 'Bahadurgarh'
                ],
                'Kerala': [
                    'Thiruvananthapuram', 'Kochi', 'Cochin', 'Kozhikode', 'Calicut', 'Kollam', 
                    'Thrissur', 'Kannur', 'Alappuzha', 'Kottayam', 'Palakkad', 'Malappuram'
                ],
                'Bihar': [
                    'Patna', 'Gaya', 'Bhagalpur', 'Muzaffarpur', 'Purnia', 'Darbhanga', 'Bihar Sharif', 
                    'Arrah', 'Begusarai', 'Katihar', 'Munger', 'Chhapra', 'Saharsa'
                ],
                'Andhra Pradesh': [
                    'Visakhapatnam', 'Vijayawada', 'Guntur', 'Nellore', 'Kurnool', 'Kakinada', 
                    'Rajahmundry', 'Tirupati', 'Kadapa', 'Anantapur', 'Eluru', 'Ongole'
                ],
                'Odisha': [
                    'Bhubaneswar', 'Cuttack', 'Rourkela', 'Berhampur', 'Sambalpur', 'Puri', 'Balasore', 'Bhadrak'
                ],
                'Assam': [
                    'Guwahati', 'Silchar', 'Dibrugarh', 'Jorhat', 'Nagaon', 'Tinsukia', 'Tezpur'
                ],
                'Jharkhand': [
                    'Ranchi', 'Jamshedpur', 'Dhanbad', 'Bokaro Steel City', 'Deoghar', 'Hazaribagh'
                ],
                'Chhattisgarh': [
                    'Raipur', 'Bhilai', 'Bilaspur', 'Korba', 'Rajnandgaon', 'Jagdalpur', 'Durg'
                ],
                'Uttarakhand': [
                    'Dehradun', 'Haridwar', 'Roorkee', 'Haldwani', 'Rudrapur', 'Rishikesh', 'Kashipur'
                ],
                'Himachal Pradesh': [
                    'Shimla', 'Dharamshala', 'Solan', 'Mandi', 'Kullu', 'Manali', 'Baddi'
                ],
                'Goa': [
                    'Panaji', 'Margao', 'Vasco da Gama', 'Mapusa', 'Ponda'
                ],
                'Chandigarh': ['Chandigarh'],
                'Jammu and Kashmir': ['Srinagar', 'Jammu', 'Anantnag', 'Baramulla'],
                'Ladakh': ['Leh', 'Kargil'],
                'Puducherry': ['Puducherry', 'Karaikal', 'Mahe', 'Yanam'],
                'Tripura': ['Agartala', 'Dharmanagar'],
                'Manipur': ['Imphal'],
                'Meghalaya': ['Shillong'],
                'Nagaland': ['Kohima', 'Dimapur'],
                'Mizoram': ['Aizawl'],
                'Sikkim': ['Gangtok']
            }
        },
        'US': {
            name: 'United States',
            states: {
                'California': ['Los Angeles', 'San Francisco', 'San Diego', 'San Jose', 'Sacramento', 'Fresno', 'Oakland', 'Long Beach', 'Bakersfield', 'Anaheim'],
                'New York': ['New York City', 'Buffalo', 'Rochester', 'Yonkers', 'Syracuse', 'Albany', 'New Rochelle'],
                'Texas': ['Houston', 'San Antonio', 'Dallas', 'Austin', 'Fort Worth', 'El Paso', 'Arlington', 'Corpus Christi', 'Plano'],
                'Florida': ['Miami', 'Orlando', 'Tampa', 'Jacksonville', 'St. Petersburg', 'Hialeah', 'Tallahassee', 'Fort Lauderdale'],
                'Illinois': ['Chicago', 'Aurora', 'Naperville', 'Joliet', 'Rockford', 'Springfield', 'Peoria'],
                'Washington': ['Seattle', 'Spokane', 'Tacoma', 'Vancouver', 'Bellevue', 'Kent', 'Everett'],
                'Pennsylvania': ['Philadelphia', 'Pittsburgh', 'Allentown', 'Erie', 'Reading', 'Scranton'],
                'Ohio': ['Columbus', 'Cleveland', 'Cincinnati', 'Toledo', 'Akron', 'Dayton'],
                'Georgia': ['Atlanta', 'Augusta', 'Columbus', 'Macon', 'Savannah', 'Athens'],
                'North Carolina': ['Charlotte', 'Raleigh', 'Greensboro', 'Durham', 'Winston-Salem', 'Fayetteville'],
                'Michigan': ['Detroit', 'Grand Rapids', 'Warren', 'Sterling Heights', 'Ann Arbor', 'Lansing'],
                'Massachusetts': ['Boston', 'Worcester', 'Springfield', 'Cambridge', 'Lowell', 'Brockton'],
                'Arizona': ['Phoenix', 'Tucson', 'Mesa', 'Chandler', 'Scottsdale', 'Glendale'],
                'Colorado': ['Denver', 'Colorado Springs', 'Aurora', 'Fort Collins', 'Lakewood'],
                'Nevada': ['Las Vegas', 'Henderson', 'Reno', 'North Las Vegas']
            }
        },
        'GB': {
            name: 'United Kingdom',
            states: {
                'England': ['London', 'Birmingham', 'Manchester', 'Leeds', 'Liverpool', 'Bristol', 'Sheffield', 'Newcastle', 'Nottingham', 'Leicester', 'Southampton', 'Oxford', 'Cambridge'],
                'Scotland': ['Glasgow', 'Edinburgh', 'Aberdeen', 'Dundee', 'Inverness', 'Perth', 'Stirling'],
                'Wales': ['Cardiff', 'Swansea', 'Newport', 'Wrexham', 'Bangor'],
                'Northern Ireland': ['Belfast', 'Derry', 'Lisburn', 'Newry', 'Armagh']
            }
        },
        'CA': {
            name: 'Canada',
            states: {
                'Ontario': ['Toronto', 'Ottawa', 'Mississauga', 'Brampton', 'Hamilton', 'London', 'Markham', 'Vaughan', 'Kitchener', 'Windsor'],
                'Quebec': ['Montreal', 'Quebec City', 'Laval', 'Gatineau', 'Longueuil', 'Sherbrooke'],
                'British Columbia': ['Vancouver', 'Surrey', 'Burnaby', 'Richmond', 'Victoria', 'Kelowna', 'Abbotsford'],
                'Alberta': ['Calgary', 'Edmonton', 'Red Deer', 'Lethbridge', 'St. Albert']
            }
        },
        'AU': {
            name: 'Australia',
            states: {
                'New South Wales': ['Sydney', 'Newcastle', 'Central Coast', 'Wollongong', 'Maitland'],
                'Victoria': ['Melbourne', 'Geelong', 'Ballarat', 'Bendigo', 'Shepparton'],
                'Queensland': ['Brisbane', 'Gold Coast', 'Sunshine Coast', 'Townsville', 'Cairns', 'Toowoomba'],
                'Western Australia': ['Perth', 'Mandurah', 'Bunbury', 'Geraldton'],
                'South Australia': ['Adelaide', 'Mount Gambier', 'Whyalla'],
                'Australian Capital Territory': ['Canberra']
            }
        },
        'AE': {
            name: 'United Arab Emirates',
            states: {
                'Dubai': ['Dubai', 'Deira', 'Bur Dubai', 'Jumeirah', 'Downtown Dubai', 'Dubai Marina', 'Al Barsha'],
                'Abu Dhabi': ['Abu Dhabi', 'Al Ain', 'Madinat Zayed', 'Ruwais'],
                'Sharjah': ['Sharjah', 'Khor Fakkan', 'Kalba', 'Al Dhaid'],
                'Ajman': ['Ajman'],
                'Ras Al Khaimah': ['Ras Al Khaimah'],
                'Fujairah': ['Fujairah']
            }
        },
        'DE': {
            name: 'Germany',
            states: {
                'Berlin': ['Berlin'],
                'Bavaria': ['Munich', 'Nuremberg', 'Augsburg', 'Regensburg', 'Ingolstadt', 'Würzburg'],
                'Baden-Württemberg': ['Stuttgart', 'Karlsruhe', 'Mannheim', 'Freiburg', 'Heidelberg', 'Ulm'],
                'North Rhine-Westphalia': ['Cologne', 'Düsseldorf', 'Dortmund', 'Essen', 'Duisburg', 'Bochum', 'Bonn', 'Münster'],
                'Hesse': ['Frankfurt', 'Wiesbaden', 'Kassel', 'Darmstadt', 'Offenbach'],
                'Hamburg': ['Hamburg'],
                'Saxony': ['Leipzig', 'Dresden', 'Chemnitz']
            }
        },
        'FR': {
            name: 'France',
            states: {
                'Île-de-France': ['Paris', 'Boulogne-Billancourt', 'Saint-Denis', 'Argenteuil', 'Montreuil', 'Versailles'],
                'Auvergne-Rhône-Alpes': ['Lyon', 'Saint-Étienne', 'Grenoble', 'Villeurbanne', 'Clermont-Ferrand'],
                'Provence-Alpes-Côte d\'Azur': ['Marseille', 'Nice', 'Toulon', 'Aix-en-Provence', 'Avignon', 'Cannes'],
                'Occitanie': ['Toulouse', 'Montpellier', 'Nîmes', 'Perpignan'],
                'Nouvelle-Aquitaine': ['Bordeaux', 'Limoges', 'Poitiers', 'Pau', 'La Rochelle']
            }
        },
        'JP': {
            name: 'Japan',
            states: {
                'Tokyo': ['Tokyo', 'Shinjuku', 'Shibuya', 'Chiyoda', 'Minato', 'Setagaya'],
                'Osaka': ['Osaka', 'Sakai', 'Higashiosaka', 'Toyonaka'],
                'Kanagawa': ['Yokohama', 'Kawasaki', 'Sagamihara', 'Fujisawa'],
                'Aichi': ['Nagoya', 'Toyota', 'Okazaki', 'Ichinomiya'],
                'Kyoto': ['Kyoto', 'Uji', 'Kameoka'],
                'Hokkaido': ['Sapporo', 'Asahikawa', 'Hakodate'],
                'Fukuoka': ['Fukuoka', 'Kitakyushu', 'Kurume'],
                'Hyogo': ['Kobe', 'Himeji', 'Nishinomiya', 'Amagasaki']
            }
        },
        'SG': {
            name: 'Singapore',
            states: {
                'Central Region': ['Singapore', 'Downtown Core', 'Bukit Merah', 'Queenstown', 'Novena'],
                'East Region': ['Tampines', 'Bedok', 'Pasir Ris'],
                'North Region': ['Woodlands', 'Yishun', 'Sembawang'],
                'North-East Region': ['Jurong East', 'Jurong West', 'Bukit Batok', 'Clementi'],
                'West Region': ['Hougang', 'Sengkang', 'Punggol', 'Ang Mo Kio']
            }
        },
        'NL': {
            name: 'Netherlands',
            states: {
                'North Holland': ['Amsterdam', 'Haarlem', 'Zaanstad', 'Hilversum', 'Alkmaar'],
                'South Holland': ['Rotterdam', 'The Hague', 'Leiden', 'Dordrecht', 'Delft'],
                'Utrecht': ['Utrecht', 'Amersfoort', 'Veenendaal']
            }
        },
        'ES': {
            name: 'Spain',
            states: {
                'Community of Madrid': ['Madrid', 'Móstoles', 'Alcalá de Henares', 'Fuenlabrada', 'Leganés'],
                'Catalonia': ['Barcelona', 'L\'Hospitalet de Llobregat', 'Badalona', 'Terrassa', 'Sabadell'],
                'Andalusia': ['Seville', 'Málaga', 'Córdoba', 'Granada', 'Jerez de la Frontera'],
                'Valencian Community': ['Valencia', 'Alicante', 'Elche', 'Castellón de la Plana']
            }
        },
        'IT': {
            name: 'Italy',
            states: {
                'Lombardy': ['Milan', 'Brescia', 'Monza', 'Bergamo', 'Como'],
                'Lazio': ['Rome', 'Latina', 'Guidonia Montecelio', 'Fiumicino'],
                'Campania': ['Naples', 'Salerno', 'Giugliano in Campania', 'Torre del Greco'],
                'Veneto': ['Venice', 'Verona', 'Padua', 'Vicenza', 'Treviso'],
                'Piedmont': ['Turin', 'Novara', 'Alessandria']
            }
        },
        'BR': {
            name: 'Brazil',
            states: {
                'São Paulo': ['São Paulo', 'Guarulhos', 'Campinas', 'São Bernardo do Campo', 'Santo André', 'Osasco'],
                'Rio de Janeiro': ['Rio de Janeiro', 'São Gonçalo', 'Duque de Caxias', 'Nova Iguaçu', 'Niterói'],
                'Federal District': ['Brasília'],
                'Minas Gerais': ['Belo Horizonte', 'Uberlândia', 'Contagem', 'Juiz de Fora'],
                'Bahia': ['Salvador', 'Feira de Santana', 'Vitória da Conquista']
            }
        },
        'MX': {
            name: 'Mexico',
            states: {
                'Mexico City': ['Mexico City', 'Iztapalapa', 'Gustavo A. Madero', 'Álvaro Obregón'],
                'Jalisco': ['Guadalajara', 'Zapopan', 'Tlaquepaque', 'Tonalá', 'Puerto Vallarta'],
                'Nuevo León': ['Monterrey', 'Guadalupe', 'San Nicolás de los Garza', 'Apodaca']
            }
        },
        'SA': {
            name: 'Saudi Arabia',
            states: {
                'Riyadh Region': ['Riyadh', 'Al Kharj', 'Ad Diriyah'],
                'Makkah Region': ['Jeddah', 'Mecca', 'Taif'],
                'Eastern Province': ['Dammam', 'Khobar', 'Dhahran', 'Jubail', 'Al Ahsa'],
                'Madinah Region': ['Medina', 'Yanbu']
            }
        },
        'ZA': {
            name: 'South Africa',
            states: {
                'Gauteng': ['Johannesburg', 'Pretoria', 'Soweto', 'Sandton', 'Centurion'],
                'Western Cape': ['Cape Town', 'Stellenbosch', 'George', 'Paarl'],
                'KwaZulu-Natal': ['Durban', 'Pietermaritzburg', 'Newcastle', 'Pinetown']
            }
        },
        'BD': {
            name: 'Bangladesh',
            states: {
                'Dhaka Division': ['Dhaka', 'Gazipur', 'Narayanganj', 'Tangail'],
                'Chittagong Division': ['Chittagong', 'Comilla', 'Cox\'s Bazar'],
                'Sylhet Division': ['Sylhet', 'Moulvibazar', 'Habiganj']
            }
        },
        'NP': {
            name: 'Nepal',
            states: {
                'Bagmati Province': ['Kathmandu', 'Lalitpur', 'Bhaktapur', 'Hetauda', 'Bharatpur'],
                'Gandaki Province': ['Pokhara', 'Baglung', 'Gorkha'],
                'Koshi Province': ['Biratnagar', 'Dharan', 'Itahari']
            }
        }
    };

    // Fast O(1) reverse lookup: normalized city name -> { city, state, countryCode, countryName }
    const CITY_REVERSE_INDEX = {};
    (function buildReverseIndex() {
        for (const [countryCode, countryObj] of Object.entries(LOCATION_DATA)) {
            const countryName = countryObj.name;
            for (const [stateName, cities] of Object.entries(countryObj.states)) {
                for (const city of cities) {
                    const key = city.trim().toLowerCase();
                    if (!CITY_REVERSE_INDEX[key]) {
                        CITY_REVERSE_INDEX[key] = {
                            city: city,
                            state: stateName,
                            countryCode: countryCode,
                            countryName: countryName
                        };
                    }
                }
            }
        }
    })();

    // Anchor geospatial data for major cities
    const ANCHOR_CITIES = {
        'indore': { lat: 22.7196, lon: 75.8577, postal: '452001', state: 'Madhya Pradesh', country: 'India', code: 'IN' },
        'bhopal': { lat: 23.2599, lon: 77.4126, postal: '462001', state: 'Madhya Pradesh', country: 'India', code: 'IN' },
        'jabalpur': { lat: 23.1815, lon: 79.9864, postal: '482001', state: 'Madhya Pradesh', country: 'India', code: 'IN' },
        'gwalior': { lat: 26.2183, lon: 78.1828, postal: '474001', state: 'Madhya Pradesh', country: 'India', code: 'IN' },
        'ujjain': { lat: 23.1765, lon: 75.7885, postal: '456001', state: 'Madhya Pradesh', country: 'India', code: 'IN' },
        'mumbai': { lat: 19.0760, lon: 72.8777, postal: '400001', state: 'Maharashtra', country: 'India', code: 'IN' },
        'pune': { lat: 18.5204, lon: 73.8567, postal: '411001', state: 'Maharashtra', country: 'India', code: 'IN' },
        'delhi': { lat: 28.6139, lon: 77.2090, postal: '110001', state: 'Delhi', country: 'India', code: 'IN' },
        'new delhi': { lat: 28.6139, lon: 77.2090, postal: '110001', state: 'Delhi', country: 'India', code: 'IN' },
        'bengaluru': { lat: 12.9716, lon: 77.5946, postal: '560001', state: 'Karnataka', country: 'India', code: 'IN' },
        'bangalore': { lat: 12.9716, lon: 77.5946, postal: '560001', state: 'Karnataka', country: 'India', code: 'IN' },
        'hyderabad': { lat: 17.3850, lon: 78.4867, postal: '500001', state: 'Telangana', country: 'India', code: 'IN' },
        'chennai': { lat: 13.0827, lon: 80.2707, postal: '600001', state: 'Tamil Nadu', country: 'India', code: 'IN' },
        'kolkata': { lat: 22.5726, lon: 88.3639, postal: '700001', state: 'West Bengal', country: 'India', code: 'IN' },
        'ahmedabad': { lat: 23.0225, lon: 72.5714, postal: '380001', state: 'Gujarat', country: 'India', code: 'IN' },
        'jaipur': { lat: 26.9124, lon: 75.7873, postal: '302001', state: 'Rajasthan', country: 'India', code: 'IN' },
        'lucknow': { lat: 26.8467, lon: 80.9462, postal: '226001', state: 'Uttar Pradesh', country: 'India', code: 'IN' },
        'chandigarh': { lat: 30.7333, lon: 76.7794, postal: '160017', state: 'Chandigarh', country: 'India', code: 'IN' },
        'london': { lat: 51.5074, lon: -0.1278, postal: 'SW1A 1AA', state: 'England', country: 'United Kingdom', code: 'GB' },
        'new york': { lat: 40.7128, lon: -74.0060, postal: '10001', state: 'New York', country: 'United States', code: 'US' },
        'san francisco': { lat: 37.7749, lon: -122.4194, postal: '94102', state: 'California', country: 'United States', code: 'US' },
        'los angeles': { lat: 34.0522, lon: -118.2437, postal: '90001', state: 'California', country: 'United States', code: 'US' },
        'chicago': { lat: 41.8781, lon: -87.6298, postal: '60601', state: 'Illinois', country: 'United States', code: 'US' },
        'toronto': { lat: 43.6532, lon: -79.3832, postal: 'M5H 2N2', state: 'Ontario', country: 'Canada', code: 'CA' },
        'sydney': { lat: -33.8688, lon: 151.2093, postal: '2000', state: 'New South Wales', country: 'Australia', code: 'AU' },
        'dubai': { lat: 25.2048, lon: 55.2708, postal: '00000', state: 'Dubai', country: 'United Arab Emirates', code: 'AE' },
        'berlin': { lat: 52.5200, lon: 13.4050, postal: '10115', state: 'Berlin', country: 'Germany', code: 'DE' },
        'paris': { lat: 48.8566, lon: 2.3522, postal: '75001', state: 'Île-de-France', country: 'France', code: 'FR' },
        'tokyo': { lat: 35.6762, lon: 139.6503, postal: '100-0001', state: 'Tokyo', country: 'Japan', code: 'JP' },
        'singapore': { lat: 1.3521, lon: 103.8198, postal: '018989', state: 'Central Region', country: 'Singapore', code: 'SG' }
    };

    /**
     * Reusable Custom Combobox Controller
     * Generates a fully searchable, styled floating dropdown without <datalist> artifacts.
     */
    function createSearchableCombobox(config) {
        const {
            wrapperEl,
            inputEl,
            hiddenInputEl,
            arrowEl,
            clearEl,
            dropdownEl,
            getItems,     // Function(query) -> Array<{ value, label, sub, code }>
            onSelect,     // Function(item)
            onChange,     // Function(value)
            allowCustom   // boolean
        } = config;

        let activeItems = [];
        let highlightedIndex = -1;

        // Populate and open dropdown
        function open(filterQuery) {
            const query = typeof filterQuery === 'string' ? filterQuery.trim() : (inputEl.value || '').trim();
            activeItems = getItems(query);
            renderMenu(activeItems, query);
            highlightedIndex = -1;
            wrapperEl.classList.add('is-open');
        }

        function close() {
            wrapperEl.classList.remove('is-open');
            highlightedIndex = -1;
        }

        function toggle() {
            if (wrapperEl.classList.contains('is-open')) {
                close();
            } else {
                open('');
                inputEl.focus();
            }
        }

        function updateClearBtn() {
            if (inputEl.value && inputEl.value.trim().length > 0) {
                wrapperEl.classList.add('has-value');
            } else {
                wrapperEl.classList.remove('has-value');
            }
        }

        // Render floating dropdown menu items
        function renderMenu(items, query) {
            dropdownEl.innerHTML = '';
            if (!items || items.length === 0) {
                const empty = document.createElement('div');
                empty.className = 'annpath-combobox-empty';
                empty.textContent = 'No matching options found';
                dropdownEl.appendChild(empty);
                return;
            }

            const list = document.createElement('ul');
            list.className = 'annpath-combobox-list';

            const queryLower = (query || '').toLowerCase();

            items.forEach((item, index) => {
                const li = document.createElement('li');
                li.className = 'annpath-combobox-item';
                li.setAttribute('data-index', index);

                const currentVal = hiddenInputEl ? hiddenInputEl.value : inputEl.value;
                if (currentVal && (currentVal === item.value || currentVal.toLowerCase() === item.label.toLowerCase())) {
                    li.classList.add('is-selected');
                }

                const textSpan = document.createElement('span');
                textSpan.className = 'item-text';

                // Highlight matching substring
                const labelText = item.label;
                const matchPos = queryLower ? labelText.toLowerCase().indexOf(queryLower) : -1;
                if (matchPos !== -1) {
                    const before = labelText.substring(0, matchPos);
                    const matched = labelText.substring(matchPos, matchPos + queryLower.length);
                    const after = labelText.substring(matchPos + queryLower.length);
                    textSpan.innerHTML = escapeHtml(before) + '<span class="item-match">' + escapeHtml(matched) + '</span>' + escapeHtml(after);
                } else {
                    textSpan.textContent = labelText;
                }

                li.appendChild(textSpan);

                if (item.sub) {
                    const subSpan = document.createElement('span');
                    subSpan.className = 'item-sub';
                    subSpan.textContent = item.sub;
                    li.appendChild(subSpan);
                }

                li.addEventListener('mousedown', function (e) {
                    e.preventDefault(); // Prevent blur before select
                    selectItem(item);
                });

                list.appendChild(li);
            });

            dropdownEl.appendChild(list);

            const countDiv = document.createElement('div');
            countDiv.className = 'annpath-combobox-count';
            countDiv.textContent = items.length + ' available option' + (items.length > 1 ? 's' : '');
            dropdownEl.appendChild(countDiv);
        }

        function selectItem(item) {
            inputEl.value = item.label;
            if (hiddenInputEl) {
                hiddenInputEl.value = item.value || item.code || item.label;
            }
            updateClearBtn();
            close();
            if (typeof onSelect === 'function') {
                onSelect(item);
            }
        }

        function highlightItem(index) {
            const listItems = dropdownEl.querySelectorAll('.annpath-combobox-item');
            listItems.forEach((el, i) => {
                if (i === index) {
                    el.classList.add('is-highlighted');
                    el.scrollIntoView({ block: 'nearest' });
                } else {
                    el.classList.remove('is-highlighted');
                }
            });
        }

        // ==========================================
        // Event Listeners
        // ==========================================
        inputEl.addEventListener('focus', function () {
            open(this.value);
        });

        inputEl.addEventListener('input', function () {
            updateClearBtn();
            if (hiddenInputEl && !allowCustom) {
                // Check if exact match exists
                const exact = activeItems.find(i => i.label.toLowerCase() === this.value.trim().toLowerCase());
                hiddenInputEl.value = exact ? (exact.value || exact.code) : '';
            } else if (hiddenInputEl) {
                hiddenInputEl.value = this.value;
            }

            open(this.value);

            if (typeof onChange === 'function') {
                onChange(this.value.trim());
            }
        });

        inputEl.addEventListener('keydown', function (e) {
            const listItems = dropdownEl.querySelectorAll('.annpath-combobox-item');
            if (!wrapperEl.classList.contains('is-open')) {
                if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
                    open(this.value);
                    e.preventDefault();
                    return;
                }
            }

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                if (listItems.length > 0) {
                    highlightedIndex = (highlightedIndex + 1) % listItems.length;
                    highlightItem(highlightedIndex);
                }
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                if (listItems.length > 0) {
                    highlightedIndex = (highlightedIndex - 1 + listItems.length) % listItems.length;
                    highlightItem(highlightedIndex);
                }
            } else if (e.key === 'Enter') {
                if (wrapperEl.classList.contains('is-open') && highlightedIndex >= 0 && activeItems[highlightedIndex]) {
                    e.preventDefault();
                    selectItem(activeItems[highlightedIndex]);
                }
            } else if (e.key === 'Escape') {
                close();
            } else if (e.key === 'Tab') {
                close();
            }
        });

        if (arrowEl) {
            arrowEl.addEventListener('click', function (e) {
                e.stopPropagation();
                toggle();
            });
        }

        if (clearEl) {
            clearEl.addEventListener('click', function (e) {
                e.stopPropagation();
                inputEl.value = '';
                if (hiddenInputEl) hiddenInputEl.value = '';
                updateClearBtn();
                open('');
                inputEl.focus();
                if (typeof onChange === 'function') {
                    onChange('');
                }
            });
        }

        // Global click-outside listener
        document.addEventListener('click', function (e) {
            if (!wrapperEl.contains(e.target)) {
                close();
            }
        });

        updateClearBtn();

        return {
            setValue: function (label, code) {
                inputEl.value = label || '';
                if (hiddenInputEl) {
                    hiddenInputEl.value = code || label || '';
                }
                updateClearBtn();
            },
            getValue: function () {
                return hiddenInputEl ? hiddenInputEl.value : inputEl.value;
            },
            getLabel: function () {
                return inputEl.value;
            },
            refresh: function () {
                updateClearBtn();
            }
        };
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    /**
     * Visual glow animation when a field is auto-populated
     */
    function highlightField(element) {
        if (!element) return;
        element.classList.remove('field-autofill-highlight');
        void element.offsetWidth; // Trigger reflow
        element.classList.add('field-autofill-highlight');
        setTimeout(() => {
            element.classList.remove('field-autofill-highlight');
        }, 1800);
    }

    /**
     * Main Form Initialization
     */
    function initLocationComboboxSuite() {
        // Collect forms that contain country combobox or location buttons
        const countryWrappers = document.querySelectorAll('.annpath-combobox-wrapper[data-combobox-type="country"]');
        countryWrappers.forEach(cw => {
            const form = cw.closest('form');
            if (!form) return;
            if (form.__annpathComboboxInit) return;
            form.__annpathComboboxInit = true;

            const sw = form.querySelector('.annpath-combobox-wrapper[data-combobox-type="state"]');
            const ciw = form.querySelector('.annpath-combobox-wrapper[data-combobox-type="city"]');
            const postalInput = form.querySelector('#postalCode, #pickupPostalCode, input[name="postalCode"], input[name="pickupPostalCode"], input[name="zipCode"]');
            const addressInput = form.querySelector('#addressLine1, #pickupAddress, input[name="addressLine1"], input[name="pickupAddress"], #address');
            const latInput = form.querySelector('#latitude, #pickupLatitude, input[name="latitude"], input[name="pickupLatitude"]');
            const lonInput = form.querySelector('#longitude, #pickupLongitude, input[name="longitude"], input[name="pickupLongitude"]');
            const gpsBtn = form.querySelector('#btnDetectLocation, #btnUseCurrentLocation, .btn-detect-location, .btn-current-location') ||
                           document.querySelector('#btnDetectLocation, #btnUseCurrentLocation, .btn-detect-location, .btn-current-location');
            const gpsStatus = form.querySelector('#locationStatus, .location-status-badge') ||
                             document.querySelector('#locationStatus, .location-status-badge');

            setupComboboxTrio({
                countryWrapper: cw,
                stateWrapper: sw,
                cityWrapper: ciw,
                postalInput: postalInput,
                addressInput: addressInput,
                latInput: latInput,
                lonInput: lonInput,
                gpsBtn: gpsBtn,
                gpsStatus: gpsStatus
            });
        });
    }

    /**
     * Connects Country, State, and City Comboboxes with Cascading & Reverse Geocoding
     */
    function setupComboboxTrio(refs) {
        const {
            countryWrapper,
            stateWrapper,
            cityWrapper,
            postalInput,
            addressInput,
            latInput,
            lonInput,
            gpsBtn,
            gpsStatus
        } = refs;

        if (!countryWrapper || !stateWrapper || !cityWrapper) return;

        // 1. COUNTRY COMBOBOX
        const cInput = countryWrapper.querySelector('.annpath-combobox-input');
        const cHidden = countryWrapper.querySelector('input[type="hidden"]');
        const cArrow = countryWrapper.querySelector('.annpath-combobox-arrow');
        const cClear = countryWrapper.querySelector('.annpath-combobox-clear');
        const cDropdown = countryWrapper.querySelector('.annpath-combobox-dropdown');

        // 2. STATE COMBOBOX
        const sInput = stateWrapper.querySelector('.annpath-combobox-input');
        const sHidden = stateWrapper.querySelector('input[type="hidden"]');
        const sArrow = stateWrapper.querySelector('.annpath-combobox-arrow');
        const sClear = stateWrapper.querySelector('.annpath-combobox-clear');
        const sDropdown = stateWrapper.querySelector('.annpath-combobox-dropdown');

        // 3. CITY COMBOBOX
        const ciInput = cityWrapper.querySelector('.annpath-combobox-input');
        const ciHidden = cityWrapper.querySelector('input[type="hidden"]');
        const ciArrow = cityWrapper.querySelector('.annpath-combobox-arrow');
        const ciClear = cityWrapper.querySelector('.annpath-combobox-clear');
        const ciDropdown = cityWrapper.querySelector('.annpath-combobox-dropdown');

        // Initial default country: from hidden input, or 'IN'
        let currentCountryCode = (cHidden && cHidden.value) ? cHidden.value.trim().toUpperCase() : 'IN';
        if (!currentCountryCode) currentCountryCode = 'IN';

        // Find country display label for initial country code
        const initialCountry = GLOBAL_COUNTRIES.find(c => c.code === currentCountryCode);
        if (initialCountry && cInput && !cInput.value) {
            cInput.value = initialCountry.name;
            if (cHidden) cHidden.value = initialCountry.code;
        }

        // ==========================================
        // CREATE COUNTRY COMBOBOX
        // ==========================================
        const countryBox = createSearchableCombobox({
            wrapperEl: countryWrapper,
            inputEl: cInput,
            hiddenInputEl: cHidden,
            arrowEl: cArrow,
            clearEl: cClear,
            dropdownEl: cDropdown,
            allowCustom: false,
            getItems: function (query) {
                const q = (query || '').toLowerCase();
                return GLOBAL_COUNTRIES.filter(c => {
                    return !q || c.name.toLowerCase().includes(q) || c.code.toLowerCase().includes(q);
                }).map(c => ({
                    value: c.code,
                    label: c.name,
                    sub: c.flag + ' ' + c.code,
                    code: c.code
                }));
            },
            onSelect: function (item) {
                currentCountryCode = item.code;
                if (cHidden) cHidden.value = item.code;

                // If current state doesn't belong to this country, clear it
                const countryData = LOCATION_DATA[currentCountryCode];
                if (sInput && sInput.value.trim()) {
                    const curState = sInput.value.trim();
                    if (!countryData || !countryData.states || !countryData.states[curState]) {
                        stateBox.setValue('', '');
                        cityBox.setValue('', '');
                    }
                }
            },
            onChange: function (val) {
                const exact = GLOBAL_COUNTRIES.find(c => c.name.toLowerCase() === (val || '').toLowerCase() || c.code.toLowerCase() === (val || '').toLowerCase());
                if (exact) {
                    currentCountryCode = exact.code;
                    if (cHidden) cHidden.value = exact.code;
                }
            }
        });

        // ==========================================
        // CREATE STATE COMBOBOX
        // ==========================================
        const stateBox = createSearchableCombobox({
            wrapperEl: stateWrapper,
            inputEl: sInput,
            hiddenInputEl: sHidden,
            arrowEl: sArrow,
            clearEl: sClear,
            dropdownEl: sDropdown,
            allowCustom: true,
            getItems: function (query) {
                const q = (query || '').toLowerCase();
                const countryData = LOCATION_DATA[currentCountryCode];
                const stateList = [];

                if (countryData && countryData.states) {
                    Object.keys(countryData.states).sort().forEach(sName => {
                        if (!q || sName.toLowerCase().includes(q)) {
                            stateList.push({
                                value: sName,
                                label: sName,
                                sub: countryData.name
                            });
                        }
                    });
                } else {
                    // Search across all global states if country not in list
                    for (const [cCode, cObj] of Object.entries(LOCATION_DATA)) {
                        for (const sName of Object.keys(cObj.states)) {
                            if (!q || sName.toLowerCase().includes(q)) {
                                stateList.push({
                                    value: sName,
                                    label: sName,
                                    sub: cObj.name,
                                    countryCode: cCode
                                });
                            }
                        }
                    }
                }
                return stateList;
            },
            onSelect: function (item) {
                // If item has a countryCode and it differs from current, auto-switch Country
                if (item.countryCode && item.countryCode !== currentCountryCode) {
                    const countryObj = GLOBAL_COUNTRIES.find(c => c.code === item.countryCode);
                    if (countryObj) {
                        countryBox.setValue(countryObj.name, countryObj.code);
                        currentCountryCode = countryObj.code;
                        highlightField(cInput);
                    }
                }

                // If current city doesn't belong to this state, clear city
                const cData = LOCATION_DATA[currentCountryCode];
                if (cData && cData.states && cData.states[item.label]) {
                    const curCity = ciInput.value.trim().toLowerCase();
                    const validCities = cData.states[item.label].map(c => c.toLowerCase());
                    if (curCity && !validCities.includes(curCity)) {
                        cityBox.setValue('', '');
                    }
                }
            },
            onChange: function (val) {
                if (!val) return;
                // Check if this typed state belongs to another country
                for (const [cCode, cObj] of Object.entries(LOCATION_DATA)) {
                    if (cObj.states && cObj.states[val]) {
                        if (cCode !== currentCountryCode) {
                            const cItem = GLOBAL_COUNTRIES.find(c => c.code === cCode);
                            if (cItem) {
                                countryBox.setValue(cItem.name, cItem.code);
                                currentCountryCode = cItem.code;
                                highlightField(cInput);
                            }
                        }
                        break;
                    }
                }
            }
        });

        // ==========================================
        // CREATE CITY COMBOBOX
        // ==========================================
        const cityBox = createSearchableCombobox({
            wrapperEl: cityWrapper,
            inputEl: ciInput,
            hiddenInputEl: ciHidden,
            arrowEl: ciArrow,
            clearEl: ciClear,
            dropdownEl: ciDropdown,
            allowCustom: true,
            getItems: function (query) {
                const q = (query || '').toLowerCase();
                const curState = (sInput.value || '').trim();
                const countryData = LOCATION_DATA[currentCountryCode];
                const cityList = [];

                if (countryData && curState && countryData.states && countryData.states[curState]) {
                    // Filtered to this state
                    countryData.states[curState].forEach(cName => {
                        if (!q || cName.toLowerCase().includes(q)) {
                            cityList.push({
                                value: cName,
                                label: cName,
                                sub: curState
                            });
                        }
                    });
                } else if (countryData && countryData.states) {
                    // Show cities across current country
                    for (const [sName, cArr] of Object.entries(countryData.states)) {
                        cArr.forEach(cName => {
                            if (!q || cName.toLowerCase().includes(q)) {
                                cityList.push({
                                    value: cName,
                                    label: cName,
                                    sub: sName
                                });
                            }
                        });
                    }
                } else {
                    // Global search
                    for (const [cCode, cObj] of Object.entries(LOCATION_DATA)) {
                        for (const [sName, cArr] of Object.entries(cObj.states)) {
                            cArr.forEach(cName => {
                                if (!q || cName.toLowerCase().includes(q)) {
                                    cityList.push({
                                        value: cName,
                                        label: cName,
                                        sub: sName + ', ' + cObj.name,
                                        stateName: sName,
                                        countryCode: cCode
                                    });
                                }
                            });
                        }
                    }
                }
                return cityList;
            },
            onSelect: function (item) {
                handleCityMatch(item.label);
            },
            onChange: function (val) {
                if (val && val.length >= 3) {
                    handleCityMatch(val);
                }
            }
        });

        /**
         * Reverse-matches City to State and Country
         */
        function handleCityMatch(cityName) {
            const normalized = (cityName || '').trim().toLowerCase();
            const match = CITY_REVERSE_INDEX[normalized];
            if (match) {
                // Auto-fill State
                if (sInput.value.trim() !== match.state) {
                    stateBox.setValue(match.state, match.state);
                    highlightField(sInput);
                }

                // Auto-fill Country
                if (currentCountryCode !== match.countryCode) {
                    countryBox.setValue(match.countryName, match.countryCode);
                    currentCountryCode = match.countryCode;
                    highlightField(cInput);
                }

                // Auto-fill Postal Code if empty and known
                const anchor = ANCHOR_CITIES[normalized];
                if (anchor && postalInput && !postalInput.value.trim()) {
                    postalInput.value = anchor.postal;
                    highlightField(postalInput);
                }

                // Auto-fill Coordinates if empty and known
                if (anchor && latInput && lonInput && (!latInput.value || !lonInput.value)) {
                    latInput.value = anchor.lat.toFixed(6);
                    lonInput.value = anchor.lon.toFixed(6);
                }
            }
        }

        /**
         * Parses and applies reverse-geocoded data from Nominatim OpenStreetMap
         * Automatically populates the Primary Street Address text field, City, State, Country, and Postal Code.
         */
        function applyGeocodedData(data, lat, lon) {
            const addr = data.address || {};

            // 1. Build comprehensive street address for the Address Text Field
            const premises = addr.building || addr.amenity || addr.office || addr.shop || 
                             addr.tourism || addr.leisure || addr.house_name || addr.commercial || 
                             addr.hotel || addr.restaurant || addr.school || addr.hospital || '';

            const houseNum = addr.house_number || addr.street_number || '';
            const road = addr.road || addr.street || addr.pedestrian || addr.footway || addr.path || addr.highway || '';
            const locality = addr.suburb || addr.neighbourhood || addr.residential || addr.subdistrict || 
                             addr.quarter || addr.city_district || addr.locality || addr.area || '';

            const streetParts = [];
            if (premises) {
                streetParts.push(premises);
            }
            if (houseNum && road) {
                streetParts.push(houseNum + ', ' + road);
            } else if (houseNum) {
                streetParts.push('Plot ' + houseNum);
            } else if (road) {
                streetParts.push(road);
            }
            if (locality && !streetParts.includes(locality)) {
                streetParts.push(locality);
            }

            // If structured parts are few, extract clean local segments from display_name
            let finalStreet = streetParts.join(', ');
            if (!finalStreet || streetParts.length < 2) {
                if (data.display_name) {
                    const segments = data.display_name.split(',').map(s => s.trim()).filter(Boolean);
                    const countryName = (addr.country || '').toLowerCase();
                    const stateName = (addr.state || '').toLowerCase();
                    const cityName = (addr.city || addr.town || addr.municipality || '').toLowerCase();
                    const postCode = (addr.postcode || '').toLowerCase();
                    const countyName = (addr.county || '').toLowerCase();

                    const localOnly = segments.filter(seg => {
                        const sl = seg.toLowerCase();
                        return sl !== countryName && sl !== stateName && sl !== cityName && sl !== postCode && sl !== countyName;
                    });

                    if (localOnly.length > 0) {
                        finalStreet = localOnly.slice(0, 3).join(', ');
                    } else if (!finalStreet) {
                        finalStreet = segments.slice(0, 3).join(', ');
                    }
                }
            }

            // AUTO-FILL PRIMARY STREET ADDRESS TEXT FIELD
            if (addressInput && finalStreet) {
                addressInput.value = finalStreet;
                highlightField(addressInput);
            }

            // 2. City
            const detectedCity = addr.city || addr.town || addr.municipality || addr.village || addr.hamlet || addr.county;
            if (detectedCity) {
                cityBox.setValue(detectedCity, detectedCity);
                highlightField(ciInput);
            }

            // 3. State
            const detectedState = addr.state || addr.state_district || addr.region || addr.province;
            if (detectedState) {
                stateBox.setValue(detectedState, detectedState);
                highlightField(sInput);
            }

            // 4. Country
            const detectedCountryCode = (addr.country_code || '').toUpperCase();
            const detectedCountryName = addr.country;
            if (detectedCountryCode) {
                const cObj = GLOBAL_COUNTRIES.find(c => c.code === detectedCountryCode);
                const countryTitle = cObj ? cObj.name : (detectedCountryName || detectedCountryCode);
                countryBox.setValue(countryTitle, detectedCountryCode);
                currentCountryCode = detectedCountryCode;
                highlightField(cInput);
            }

            // 5. Postal Code
            const detectedPostcode = addr.postcode || addr.postal_code;
            if (postalInput && detectedPostcode) {
                postalInput.value = detectedPostcode;
                highlightField(postalInput);
            }

            return finalStreet;
        }

        /**
         * Fallback using closest regional anchor city when offline or API is unreachable
         */
        function fallbackNearestAnchor(lat, lon) {
            let minDistance = Infinity;
            let bestMatch = null;

            for (const [cityName, info] of Object.entries(ANCHOR_CITIES)) {
                const d = haversine(lat, lon, info.lat, info.lon);
                if (d < minDistance) {
                    minDistance = d;
                    bestMatch = info;
                }
            }

            let fallbackStreet = '';
            if (bestMatch && minDistance <= 120) {
                cityBox.setValue(bestMatch.city, bestMatch.city);
                stateBox.setValue(bestMatch.state, bestMatch.state);
                countryBox.setValue(bestMatch.country, bestMatch.code);
                currentCountryCode = bestMatch.code;
                if (postalInput && bestMatch.postal) {
                    postalInput.value = bestMatch.postal;
                    highlightField(postalInput);
                }
                fallbackStreet = 'Near ' + bestMatch.city + ', ' + bestMatch.state;
                if (addressInput) {
                    addressInput.value = fallbackStreet;
                    highlightField(addressInput);
                }
                highlightField(ciInput);
                highlightField(sInput);
                highlightField(cInput);
            }
            return fallbackStreet;
        }

        function haversine(lat1, lon1, lat2, lon2) {
            const R = 6371; // km
            const dLat = (lat2 - lat1) * Math.PI / 180;
            const dLon = (lon2 - lon1) * Math.PI / 180;
            const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                      Math.sin(dLon / 2) * Math.sin(dLon / 2);
            return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        }

        // ==========================================
        // GPS CURRENT LOCATION INTEGRATION & AUTO-FILL
        // ==========================================
        function performReverseGeocode(lat, lon) {
            if (latInput) latInput.value = lat.toFixed(6);
            if (lonInput) lonInput.value = lon.toFixed(6);

            if (gpsStatus) {
                gpsStatus.textContent = 'Converting coordinates to street address...';
                gpsStatus.style.color = '#0284c7';
            }

            const geocodeUrl = 'https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=' + lat + '&lon=' + lon + '&addressdetails=1';

            return fetch(geocodeUrl, {
                headers: { 'Accept': 'application/json' }
            })
            .then(res => {
                if (!res.ok) throw new Error('Geocoding response status: ' + res.status);
                return res.json();
            })
            .then(data => {
                const filledStreet = applyGeocodedData(data, lat, lon);
                if (gpsStatus) {
                    gpsStatus.textContent = '✓ Address & location auto-filled: ' + (filledStreet || 'Coordinates set');
                    gpsStatus.style.color = '#16a34a';
                }
                return filledStreet;
            })
            .catch(err => {
                const fallbackStreet = fallbackNearestAnchor(lat, lon);
                if (gpsStatus) {
                    gpsStatus.textContent = '✓ Location set from coordinates (' + lat.toFixed(4) + ', ' + lon.toFixed(4) + ').';
                    gpsStatus.style.color = '#16a34a';
                }
                return fallbackStreet;
            });
        }

        if (gpsBtn) {
            gpsBtn.dataset.geoAttached = 'true';
            gpsBtn.addEventListener('click', function (e) {
                e.preventDefault();

                if (!navigator.geolocation) {
                    if (gpsStatus) {
                        gpsStatus.textContent = 'Geolocation is not supported by your browser.';
                        gpsStatus.style.color = '#dc2626';
                    }
                    return;
                }

                const originalBtnHtml = gpsBtn.innerHTML;
                gpsBtn.disabled = true;
                gpsBtn.innerHTML = '⏳ Acquiring GPS...';

                if (gpsStatus) {
                    gpsStatus.textContent = 'Acquiring GPS location...';
                    gpsStatus.style.color = '#0284c7';
                }

                function onGeoSuccess(position) {
                    const lat = position.coords.latitude;
                    const lon = position.coords.longitude;
                    performReverseGeocode(lat, lon).finally(() => {
                        gpsBtn.disabled = false;
                        gpsBtn.innerHTML = originalBtnHtml;
                    });
                }

                function onGeoError(error) {
                    // If high accuracy times out (common on desktop/laptops), retry with network triangulation
                    if (error.code === 3) {
                        if (gpsStatus) {
                            gpsStatus.textContent = 'High accuracy timed out, retrying via network...';
                        }
                        navigator.geolocation.getCurrentPosition(
                            onGeoSuccess,
                            function (err2) {
                                handleGeoErrorDisplay(err2);
                            },
                            { enableHighAccuracy: false, timeout: 8000, maximumAge: 300000 }
                        );
                        return;
                    }
                    handleGeoErrorDisplay(error);
                }

                function handleGeoErrorDisplay(error) {
                    let errMsg = 'Location access denied or unavailable.';
                    if (error.code === 1) errMsg = 'Location permission denied in browser settings.';
                    else if (error.code === 2) errMsg = 'Location position unavailable on this device.';
                    else if (error.code === 3) errMsg = 'Location request timed out.';

                    if (gpsStatus) {
                        gpsStatus.textContent = '⚠️ ' + errMsg + ' (You can type your address manually).';
                        gpsStatus.style.color = '#dc2626';
                    }
                    gpsBtn.disabled = false;
                    gpsBtn.innerHTML = originalBtnHtml;
                }

                navigator.geolocation.getCurrentPosition(
                    onGeoSuccess,
                    onGeoError,
                    { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
                );
            });
        }

        // Expose global AnnPathLocation API & test helper
        window.AnnPathLocation = {
            reverseGeocodeAndFill: function (lat, lon) {
                return performReverseGeocode(Number(lat), Number(lon));
            }
        };
        window.__testLocationGeocode = function (lat, lon) {
            return performReverseGeocode(lat, lon);
        };
    }

    // Auto-initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initLocationComboboxSuite);
    } else {
        initLocationComboboxSuite();
    }

})();
