/*
 * 1 bbox = 0.01
 * 2 source
 * 3 dest
 * 4 source
 * 5 dest
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
		pgr_dijkstra(concat(
			'SELECT osm_id::INTEGER as id, source::INTEGER, target::INTEGER, cost
			FROM ways as r,
	        	(SELECT
					ST_Expand(ST_Extent(the_geom),', ?, ') as box
				FROM
					ways as l1
				WHERE l1.source = 
					(SELECT id::int FROM ways_vertices_pgr WHERE osm_id = ', ?, ')
				OR l1.target =
					(SELECT id::int FROM ways_vertices_pgr WHERE osm_id = ', ?, ')) as box
	        WHERE r.the_geom && box.box'),
		(SELECT
			id::int 
		FROM
			ways_vertices_pgr
		WHERE osm_id = ?),
		(SELECT
			id::int
		FROM 
			ways_vertices_pgr
		WHERE
			osm_id = ?),
		false, false
	) as r 
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