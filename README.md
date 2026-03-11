# Android Flight Planner

A Jetpack Compose Android app for searching flights from an origin city/airport to Damascus or nearby airports (Beirut, Amman, Aleppo).

## Features
- Origin search by city or IATA code with nearby-origin resolution.
- Destination fan-out to DAM / BEY / AMM / ALP.
- Passenger inputs:
  - Adults
  - Children 0-2 years
  - Children 2-12 years
- One-way or round-trip date support.
- Preference selection for direct-only or transit-allowed itineraries.
- Multi-provider aggregation in-app (Skyscanner, Google Flights via SERP API, and portal fallback provider).
- Result list shown inside the app with price labels.
- Arabic localization for all end-user UI labels/messages (via `values-ar/strings.xml`).
- Tapping a result opens the external booking portal deep link.

## API setup
Set API keys in `app/build.gradle.kts` for production use:
- `SERP_API_KEY` for Google Flights through SerpApi.
- `SKYSCANNER_API_KEY` for Skyscanner partner API.

If keys are empty, the app still works using the fallback known-portal provider so you can test UX end-to-end.

## Notes
- Some provider endpoints require commercial API agreements.
- The app is structured so each provider can be replaced with your own backend proxy if needed.
