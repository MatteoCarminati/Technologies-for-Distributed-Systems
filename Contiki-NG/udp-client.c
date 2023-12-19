#include "contiki.h"
#include "net/routing/routing.h"
#include "random.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"

#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define WITH_SERVER_REPLY 1
#define UDP_CLIENT_PORT 8765
#define UDP_SERVER_PORT 5678

static struct simple_udp_connection udp_conn;

#define SEND_INTERVAL (60 * CLOCK_SECOND)
#define FAKE_TEMPS 5

static struct simple_udp_connection udp_conn;

/*---------------------------------------------------------------------------*/
PROCESS(udp_client_process, "UDP client");
AUTOSTART_PROCESSES(&udp_client_process);
/*---------------------------------------------------------------------------*/
static unsigned
get_temperature()
{
	static unsigned fake_temps[FAKE_TEMPS] = {30, 25, 20, 15, 10};
	return fake_temps[random_rand() % FAKE_TEMPS];
}
/*---------------------------------------------------------------------------*/
static void
udp_rx_callback(struct simple_udp_connection *c,
				const uip_ipaddr_t *sender_addr,
				uint16_t sender_port,
				const uip_ipaddr_t *receiver_addr,
				uint16_t receiver_port,
				const uint8_t *data,
				uint16_t datalen)
{
	float average = *(float*)data; 
	LOG_INFO("Recieved high temp alert with average %f\n",average);
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_client_process, ev, data)
{
	static struct etimer timer;
	uip_ipaddr_t dest_ipaddr;

	PROCESS_BEGIN();

	/* Initialize UDP connection */
	simple_udp_register(&udp_conn, UDP_CLIENT_PORT, NULL,
						UDP_SERVER_PORT, udp_rx_callback);
	etimer_set(&timer,10*CLOCK_SECOND);
	while(1){
		PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&timer));
		if(NETSTACK_ROUTING.node_is_reachable() && NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr)) {
			unsigned temp = get_temperature();
			LOG_INFO("Sending temperature %u", temp);
			LOG_INFO_("\n");
			simple_udp_sendto(&udp_conn, &temp, sizeof(temp), &dest_ipaddr);
			etimer_set(&timer,10*CLOCK_SECOND);
		} else {
			LOG_INFO("Not reachable yet\n");
			etimer_set(&timer,10*CLOCK_SECOND);
		}
	}

	PROCESS_END();
}
/*---------------------------------------------------------------------------*/ 
