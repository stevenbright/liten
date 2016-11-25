
import $ from 'jquery'; //var $ = require('jquery');

import {setUpPaginationHandlers} from './logic/pagination';
import {setUpFavoritesHandlers} from './logic/favorites';

// Set up global handlers
setUpPaginationHandlers();
setUpFavoritesHandlers();

$(document).ready(function () {
  $('#javascript-alert').remove();
  console.log('BTF loaded');
});
