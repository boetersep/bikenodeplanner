# Import and optimize data

### Import OSM data

```shell
wget --progress=dot:mega -O nlrcn.osm https://overpass-api.de/api/interpreter?data=%5Bout%3Axml%5D%5Btimeout%3A12000%5D%3Barea%283600047796%29%2D%3E%2EsearchArea%3B%28relation%5B%22type%22%3D%22network%22%5D%5B%22network%22%3D%22rcn%22%5D%28area%2EsearchArea%29%3Bnode%28r%29%2D%3E%2Enodes%3Brel%28r%29%3Bway%28r%29%3Bnode%28w%29%3B%29%3Bout%20body%3B%3E%3Bout%20skel%20qt%3B%0A
createdb rcn
psql rcn -c "CREATE USER rcn WITH PASSWORD 'rcn'"
psql rcn -c "GRANT ALL PRIVILEGES ON DATABASE rcn to rcn"
psql rcn -c "create extension postgis"
psql rcn -c "create extension pgrouting"
echo "node rcn_ref text linear" > rcn_ref.style
export PGPASS=rcn
osm2pgsql nlrcn.osm --database rcn --latlong --style rcn_ref.style
osm2pgrouting --f nlrcn.osm --dbname rcn --username rcn --clean --password rcn
psql rcn -c "CREATE INDEX ways_osm_id_idx ON ways (osm_id)"
psql rcn -c "CREATE INDEX planet_osm_id_idx ON planet_osm_point (osm_id)"
psql rcn -c "ALTER TABLE ways ALTER COLUMN source TYPE integer"
psql rcn -c "ALTER TABLE ways ALTER COLUMN target TYPE integer"
```

### OSM overpass API query

```
[out:xml][timeout:12000];
{{geocodeArea:Nederland}}->.searchArea;
(
  relation
    ["type"="network"]
    ["network"="rcn"]
  (area.searchArea);
  node(r)->.nodes;
  rel(r);
  way(r);
  node(w);
);
// print results
out body;
>;
out skel qt;
```
