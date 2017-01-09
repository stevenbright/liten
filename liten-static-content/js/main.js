
import $ from 'jquery';

import {setUpDataLoaderHandlers} from './logic/data-loader';
import {setUpFavoritesHandlers} from './logic/favorites';
import {setUpCustomDispatcher} from './logic/custom-dispatcher'

// Set up global handlers
setUpDataLoaderHandlers();
setUpFavoritesHandlers();

// Set up custom page dispatcher code
setUpCustomDispatcher();

$(document).ready(function () {
  $('#javascript-alert').remove();
  console.log('BTF loaded #1');
});

// make jQuery globally available (for bootstrap)
window.jQuery = $;
