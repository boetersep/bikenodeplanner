/*
 * 1 left
 * 2 bottom
 * 3 right
 * 4 top
 */
SELECT array_to_json(array_agg(bikenoderows)) FROM (
SELECT 
	p.rcn_ref as bikepoint,
	p.osm_id as rcn_osm_id,
	ST_AsGeoJSON(p.way)::json as rcn_point
FROM
	planet_osm_point p
WHERE p.way && ST_MakeEnvelope(?, ?, ?, ?)
) as bikenoderows;