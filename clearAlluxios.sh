#!bin/sh
ssh root@136.145.77.68 'rm -R /root/alluxio-2.8.1/underFSStorage/.alluxio_ufs_persistence'
ssh root@136.145.77.85 'rm -R /root/alluxio-2.8.1/underFSStorage/.alluxio_ufs_persistence'
ssh root@136.145.77.117 'rm -R /root/alluxio-2.8.1/underFSStorage/.alluxio_ufs_persistence'
ssh root@136.145.77.72 'rm -R /root/alluxio-2.8.1/underFSStorage/.alluxio_ufs_persistence'
docker restart redisReal
docker restart redis_results
