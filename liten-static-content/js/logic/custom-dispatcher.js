
import $ from 'jquery';

const PATHNAME_HANDLERS = {

  '/g/cat/index': function catalogIndexHandler() {
    console.log("Activating catalog index page handler");
  }
}

export function setUpCustomDispatcher() {
  const pathname = window.location.pathname;

  if (PATHNAME_HANDLERS.hasOwnProperty(pathname)) {
    PATHNAME_HANDLERS[pathname]();
  }
}

