/*
 * 1 source
 * 2 dest
 */ 
SELECT array_to_json(array_agg(routerows)) FROM (
	SELECT
		p.rcn_ref as bikepoint,
		g.name as way_name,
		g.length_m as way_length,
		ST_AsGeoJSON(g.the_geom)::json as way_line,
		p.osm_id as rcn_osm_id,
		ST_AsGeoJSON(p.way)::json as rcn_point
	FROM
		pgr_bdAstar('
			SELECT osm_id::INTEGER as id,
			source::INTEGER,
			target::INTEGER,
			ST_length(the_geom) AS cost,
			x1,
			y1,
			x2,
			y2
		FROM
			ways', 
			(SELECT id::int FROM ways_vertices_pgr WHERE osm_id = ?),
			(SELECT id::int FROM ways_vertices_pgr WHERE osm_id = ?), 
			false, false
	)  as r 
	INNER JOIN 
		ways as g 
	ON
		r.id2 = g.osm_id
	LEFT JOIN
		planet_osm_point p
	ON
		p.osm_id = g.target_osm
	OR
		p.osm_id = g.source_osm
	ORDER BY
		seq
) as routerows

