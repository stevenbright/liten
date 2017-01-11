
import $ from 'jquery';

function catalogIndexHandler() {
  console.log("Activating catalog index page handler");
}

const PATHNAME_HANDLERS = {

  '/g/cat/index': catalogIndexHandler
}

export function setUpCustomDispatcher() {
  const pathname = window.location.pathname;

  if (PATHNAME_HANDLERS.hasOwnProperty(pathname)) {
    PATHNAME_HANDLERS[pathname]();
  }
}

